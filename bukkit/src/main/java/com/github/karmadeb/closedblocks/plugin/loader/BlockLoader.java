package com.github.karmadeb.closedblocks.plugin.loader;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.provider.block.ClosedBlockSettings;
import com.github.karmadeb.closedblocks.plugin.provider.block.type.elevator.ElevatorBlock;
import com.github.karmadeb.closedblocks.plugin.provider.block.type.mine.MineBlock;
import com.github.karmadeb.closedblocks.plugin.util.JsonUtils;
import com.github.karmadeb.kson.element.JsonArray;
import com.github.karmadeb.kson.element.JsonElement;
import com.github.karmadeb.kson.element.JsonObject;
import com.github.karmadeb.kson.parser.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class BlockLoader {

    private final ClosedBlocksPlugin plugin;
    private final ClosedBlocksAPI api;

    public BlockLoader(final ClosedBlocksPlugin plugin, final ClosedBlocksAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void load() {
        Path storageDirectory = this.plugin.getDataPath().resolve("storage");
        if (!Files.exists(storageDirectory))
            return;

        forEachInDirectory(storageDirectory, this::handlePlayerDirectory);
    }

    private void handlePlayerDirectory(final Path file) {
        if (!Files.isDirectory(file))
            return;

        String rawPlayerUUID = file.getFileName().toString();
        UUID playerUUID = fromTrimmed(rawPlayerUUID);
        if (playerUUID == null) return;

        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUUID);
        forEachInDirectory(file, (worldFolder) -> handleWorldDirectory(worldFolder, player));
    }

    private void handleWorldDirectory(final Path file, final OfflinePlayer player) {
        if (!Files.isDirectory(file))
            return;

        String rawWorldUUID = file.getFileName().toString();
        UUID worldUID = fromTrimmed(rawWorldUUID);
        if (worldUID == null) return;

        World world = Bukkit.getWorld(worldUID);
        if (world == null) return;

        Path worldData = file.resolve("data.json");
        readWorldData(player, world, worldData);
    }

    private void readWorldData(final OfflinePlayer owner, final World world, final Path data) {
        if (!Files.exists(data))
            return;

        try(InputStream stream = Files.newInputStream(data)) {
            JsonParser parser = JsonParser.create(stream);
            JsonElement element = parser.resolve();

            if (!element.isObject())
                return;

            JsonObject object = element.getAsObject();
            for (String rawX : object.getKeys()) {
                JsonElement xElement = object.get(rawX);
                if (!xElement.isObject())
                    continue;

                JsonObject xObject = xElement.getAsObject();
                try {
                    int x = Integer.parseInt(rawX);
                    handleZAxis(owner, world, x, xObject);
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to read file " + data, ex);
        }
    }

    private void handleZAxis(final OfflinePlayer owner, final World world, final int x, final JsonObject xObject) {
        for (String rawZ : xObject.getKeys()) {
            JsonElement zElement = xObject.get(rawZ);

            if (!zElement.isObject())
                continue;

            JsonObject zObject = zElement.getAsObject();
            try {
                int z = Integer.parseInt(rawZ);

                for (BlockType<?> type : BlockType.values())
                    loadBlocks(owner, world, x, z, zObject, type);
            } catch (NumberFormatException ignored) {}
        }
    }

    private void loadBlocks(final OfflinePlayer player, final World world, final int x, final int z, final JsonObject zObject, final BlockType<?> type) {
        JsonObject object = JsonUtils.getJsonObject(zObject, type.plural());
        if (object == null) return;

        if (type.equals(BlockType.ELEVATOR))
            handleYAxisElevators(player, world, x, z, object);

        if (type.equals(BlockType.MINE))
            handleYAxisMines(player, world, x, z, object);
    }

    private void handleYAxisElevators(final OfflinePlayer owner, final World world, final int x, final int z, final JsonObject elevators) {
        List<ElevatorBlock> parsedElevators = new ArrayList<>();
        AtomicInteger floors = new AtomicInteger();

        int enabledLevels = 0;
        for (String rawY : elevators.getKeys()) {
            try {
                int y = Integer.parseInt(rawY);

                JsonElement yElement = elevators.get(rawY);
                if (!yElement.isObject())
                    continue;

                JsonObject yObject = yElement.getAsObject();

                ClosedBlockSettings settings = createSettings(yObject);
                if (settings == null) continue;

                ElevatorBlock block = new ElevatorBlock(owner, world, x, y, z, floors, settings, this.plugin);

                if (settings.isEnabled())
                    enabledLevels++;

                parsedElevators.add(block);
            } catch (NumberFormatException ignored) {}
        }
        parsedElevators.sort(Comparator.comparingInt(ElevatorBlock::getY));

        floors.set(enabledLevels);
        ElevatorBlock previous = null;
        AtomicInteger vLevel = new AtomicInteger();
        for (int i = 0; i < parsedElevators.size(); i++) {
            previous = applyElevator(world, parsedElevators, i, vLevel, previous);
        }

        this.api.getBlockStorage().addAll(parsedElevators);
    }

    private void handleYAxisMines(final OfflinePlayer owner, final World world, final int x, final int z, final JsonObject mines) {
        List<MineBlock> parsedMines = new ArrayList<>();
        for (String rawY : mines.getKeys()) {
            try {
                int y = Integer.parseInt(rawY);

                JsonElement yElement = mines.get(rawY);
                if (!yElement.isObject())
                    continue;

                JsonObject yObject = yElement.getAsObject();

                Float power = JsonUtils.getFloat(yObject, "power");
                Boolean fire = JsonUtils.getBoolean(yObject, "fire");
                Boolean defused = JsonUtils.getBoolean(yObject, "defused");

                if (power == null || fire == null | defused == null)
                    continue;

                ClosedBlockSettings settings = createSettings(yObject);
                if (settings == null) continue;

                MineBlock block = new MineBlock(owner, world, x, y, z, power, fire, defused, settings, this.plugin);

                Block bBlock = world.getBlockAt(x, y, z);
                bBlock.setMetadata("closed_type", new FixedMetadataValue(this.plugin, "mine"));

                parsedMines.add(block);
            } catch (NumberFormatException ignored) {}
        }

        parsedMines.sort(Comparator.comparingInt(MineBlock::getY));
        this.api.getBlockStorage().addAll(parsedMines);
    }

    private void forEachInDirectory(final Path directory, final Consumer<Path> action) {
        try(Stream<Path> files = Files.list(directory)) {
            files.forEach(action);
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to read directory " + directory, ex);
        }
    }

    private UUID fromTrimmed(final String raw) {
        try {
            String formattedUUID = raw.replaceFirst(
                    "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                    "$1-$2-$3-$4-$5"
            );
            return UUID.fromString(formattedUUID);
        } catch (Throwable ex) {
            return null;
        }
    }

    private Set<UUID> mapViewers(final JsonArray array) {
        Set<UUID> set = new LinkedHashSet<>();

        array.forEach((instance) -> {
            if (instance.isNull()) return;
            String raw = instance.getAsString();
            if (raw == null) return;

            try {
                UUID id = UUID.fromString(raw);
                set.add(id);
            } catch (Throwable ignored) {}
        });

        return set;
    }

    private ClosedBlockSettings createSettings(final JsonObject yObject) {
        String name = JsonUtils.getString(yObject, "name");
        String disguise = JsonUtils.getString(yObject, "disguise");
        Boolean enabled = JsonUtils.getBoolean(yObject, "enabled");
        Boolean visible = JsonUtils.getBoolean(yObject, "visible");
        JsonArray viewers = JsonUtils.getArray(yObject, "viewers");

        if (name == null || disguise == null || enabled == null ||
                visible == null || viewers == null) return null;

        Set<UUID> viewersUUIDs = mapViewers(viewers);

        return new ClosedBlockSettings(this.plugin, name, disguise, enabled, visible, viewersUUIDs);
    }

    private @NotNull ElevatorBlock applyElevator(World world, List<ElevatorBlock> parsedElevators, int i, AtomicInteger vLevel, ElevatorBlock previous) {
        ElevatorBlock block = parsedElevators.get(i);
        block.setFloor(vLevel.getAndIncrement());

        if (!block.getSettings().isEnabled())
            vLevel.set(vLevel.get() - 1);

        if (previous != null)
            previous.setNext(block);

        block.setPrevious(previous);
        previous = block;

        Block blockAt = world.getBlockAt(block.getX(), block.getY(), block.getZ());
        blockAt.setMetadata("closed_type", new FixedMetadataValue(this.plugin, BlockType.ELEVATOR.singular()));
        return previous;
    }
}