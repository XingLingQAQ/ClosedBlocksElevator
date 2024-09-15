package com.github.karmadeb.closedblocks.plugin.provider.block;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.data.SaveData;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.util.JsonUtils;
import com.github.karmadeb.kson.element.JsonArray;
import com.github.karmadeb.kson.element.JsonElement;
import com.github.karmadeb.kson.element.JsonObject;
import com.github.karmadeb.kson.printer.JsonPrinter;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;

public abstract class BlockSaveData<T extends ClosedBlock> implements SaveData {

    protected final ClosedBlocksPlugin plugin;
    protected final T block;

    public BlockSaveData(final ClosedBlocksPlugin plugin, final T block) {
        this.plugin = plugin;
        this.block = block;
    }

    protected final boolean save() {
        return this.save(null);
    }

    protected final boolean save(final Consumer<JsonObject> onBeforeSave) {
        Path file = this.getBlockFile();

        JsonElement element = JsonUtils.readFile(file);
        if (!element.isObject())
            element = new JsonObject();

        JsonObject root = element.getAsObject();

        String x = String.valueOf(this.block.getX());
        String y = String.valueOf(this.block.getY());
        String z = String.valueOf(this.block.getZ());

        JsonObject xObject = getAsObject(root, x);
        JsonObject zObject = getAsObject(xObject, z);
        JsonObject typeObject = getAsObject(zObject, this.block.getType().plural());
        JsonObject yObject = getAsObject(typeObject, y);

        BlockSettings settings = this.block.getSettings();
        yObject.set("name", settings.getName());
        yObject.set("disguise", settings.getDisguise());
        yObject.set("enabled", settings.isEnabled());
        yObject.set("visible", settings.isVisible());

        writeViewers(settings, yObject);
        if (onBeforeSave != null)
            onBeforeSave.accept(yObject);

        return saveToFile(file, root);
    }

    /**
     * Tries to remove the block
     *
     * @return if the block was removed
     */
    @Override
    public boolean removeBlockData() {
        Path file = this.getBlockFile();

        JsonElement element = JsonUtils.readFile(file);
        if (!element.isObject())
            element = new JsonObject();

        JsonObject root = element.getAsObject();

        String x = String.valueOf(this.block.getX());
        String y = String.valueOf(this.block.getY());
        String z = String.valueOf(this.block.getZ());

        JsonObject xObject = getAsObject(root, x);
        JsonObject zObject = getAsObject(xObject, z);
        JsonObject typeObject = getAsObject(zObject, this.block.getType().plural());

        typeObject.remove(y);
        if (xObject.isEmpty()) {
            xObject.destroy();
        }

        return saveToFile(file, root);
    }

    /**
     * Get if the block save data
     * exists
     *
     * @return if the block exists
     */
    @Override
    public boolean exists() {
        Path file = this.getBlockFile();

        JsonElement element = JsonUtils.readFile(file);
        if (!element.isObject())
            element = new JsonObject();

        JsonObject root = element.getAsObject();

        String x = String.valueOf(this.block.getX());
        String y = String.valueOf(this.block.getY());
        String z = String.valueOf(this.block.getZ());

        JsonObject xObject = getAsObject(root, x);
        JsonObject zObject = getAsObject(xObject, z);
        JsonObject typeObject = getAsObject(zObject, this.block.getType().plural());

        return typeObject.has(y);
    }

    private boolean saveToFile(final Path file, final JsonObject root) {
        try(Writer writer = Files.newBufferedWriter(file)) {
            JsonPrinter<?> printer = root.getJsonPrinter();
            printer.print(1, writer);

            return true;
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to save block data", ex);
            return false;
        }
    }

    private void writeViewers(final BlockSettings settings, final JsonObject target) {
        JsonArray array = new JsonArray();
        for (OfflinePlayer viewer : settings.getViewers())
            array.add(viewer.getUniqueId().toString());

        target.set("viewers", array);
    }

    private JsonObject getAsObject(final JsonObject root, final String key) {
        JsonElement element = root.get(key);
        if (!element.isObject()) {
            element = new JsonObject();
            root.set(key, element);
        }

        return element.getAsObject();
    }

    private Path getBlockDirectory(final ClosedBlock block) {
        OfflinePlayer owner = block.getOwner();
        String ownerId = owner.getUniqueId().toString().replace("-", "");

        return plugin.getDataPath().resolve("storage").resolve(ownerId);
    }

    private Path getBlockFile() {
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
                Files.write(file, "{}".getBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        return file;
    }
}