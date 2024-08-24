package com.github.karmadeb.closedblocks.plugin.provider.block;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.SaveData;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.provider.storage.ClosedBlocksStorage;
import es.karmadev.api.kson.JsonArray;
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.JsonObject;
import es.karmadev.api.kson.KsonException;
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.kson.io.JsonWriter;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class BlockSaveData implements SaveData {

    private final ClosedBlocksPlugin plugin;
    private final ClosedBlock block;

    public BlockSaveData(final ClosedBlocksPlugin plugin, final ClosedBlock block) {
        this.plugin = plugin;
        this.block = block;
    }

    /**
     * Tries to save the block
     *
     * @return if the block was saved
     */
    @Override
    public boolean saveBlockData() {
        Path blockFile = getBlockFile(this.block);
        try(InputStream stream = Files.newInputStream(blockFile)) {
            JsonInstance instance = getInstanceOfStream(stream);
            JsonObject typeObject = extractTypeObject(instance);

            int y = block.getY();

            JsonObject yPos = tryGetObject(typeObject, String.valueOf(y));
            BlockSettings settings = block.getSettings();

            yPos.put("name", settings.getName());
            yPos.put("disguise", settings.getDisguise());
            yPos.put("enabled", settings.isEnabled());
            yPos.put("visible", settings.isVisible());
            if (yPos.hasChild("particles"))
                yPos.removeChild("particles");

            JsonArray array = JsonArray.newArray(getJsonFullPath(yPos), "viewers");
            settings.getViewers().forEach((e) -> array.add(e.toString()));
            yPos.put("viewers", array);

            try (Writer writer = Files.newBufferedWriter(blockFile)) {
                JsonWriter jWriter = new JsonWriter(instance);
                jWriter.export(writer);
            }

            return true;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save block data", ex);
            return false;
        }
    }

    /**
     * Tries to remove the block
     *
     * @return if the block was removed
     */
    @Override
    public boolean removeBlockData() {
        Path blockFile = getBlockFile(this.block);
        try(InputStream stream = Files.newInputStream(blockFile)) {
            JsonInstance instance = getInstanceOfStream(stream);
            JsonObject typeObject = extractTypeObject(instance);

            int y = block.getY();

            if (!typeObject.hasChild(String.valueOf(y)))
                return true;

            typeObject.removeChild(String.valueOf(y));
            try (Writer writer = Files.newBufferedWriter(blockFile)) {
                JsonWriter jWriter = new JsonWriter(instance);
                jWriter.export(writer);
            }

            World world = block.getWorld();
            Block blockAt = world.getBlockAt(block.getX(), y, block.getZ());
            blockAt.removeMetadata("closed_type", plugin);

            return true;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save block data", ex);
            return false;
        }
    }

    private JsonInstance getInstanceOfStream(final InputStream stream) {
        JsonInstance jsonElement = null;
        try {
            jsonElement = JsonReader.read(stream);
        } catch (KsonException ignored) {}

        if (jsonElement == null || !jsonElement.isObjectType())
            jsonElement = JsonObject.newObject("", "");

        return jsonElement;
    }

    private JsonObject extractTypeObject(JsonInstance jsonElement) {
        JsonObject object = jsonElement.asObject();

        int x = block.getX();
        int z = block.getZ();

        JsonObject xPos = tryGetObject(object, String.valueOf(x));
        JsonObject zPos = tryGetObject(xPos, String.valueOf(z));

        JsonObject typeObject = JsonObject.newObject(String.format("%d.%d", x, z), "unknown");
        if (block instanceof Elevator) {
            typeObject = tryGetObject(zPos, "elevators");
        }

        return typeObject;
    }

    private JsonObject tryGetObject(final JsonObject instance, final String key) {
        JsonInstance element = instance.getChild(key);
        if (element.isNull() || !element.isObjectType()) {
            JsonObject obj = JsonObject.newObject(getJsonFullPath(instance), String.valueOf(key));
            instance.put(key, obj);

            return obj;
        }

        return element.asObject();
    }

    private String getJsonFullPath(final JsonObject object) {
        String path = object.getPath();
        String key = object.getKey();

        if (path == null || path.trim().isEmpty())
            return key;

        if (key.trim().isEmpty())
            return path;

        return String.format("%s.%s", path, key);
    }

    private Path getBlockDirectory(final ClosedBlock block) {
        OfflinePlayer owner = block.getOwner();
        String ownerId = owner.getUniqueId().toString().replace("-", "");

        return plugin.getDataPath().resolve("storage").resolve(ownerId);
    }

    private Path getBlockFile(final ClosedBlock block) {
        Path directory = getBlockDirectory(block);
        World world = block.getWorld();
        Path worldDir = directory.resolve(world.getUID().toString().replace("-", ""));

        if (!Files.exists(worldDir)) {
            try {
                Files.createDirectories(worldDir);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        Path file = worldDir.resolve("data.json");
        if (!Files.exists(file)) {
            try {
                Files.createFile(file);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return file;
    }
}