package com.github.karmadeb.closedblocks.api.storage;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents the storage of all blocks
 */
public abstract class BlockStorage {

    /**
     * Get if a block is a closed block
     * instance
     *
     * @param block the placed block
     * @return if the block is a closed block
     */
    public abstract boolean isClosedBlock(final @NotNull Block block);

    /**
     * Get a closed block from a placed
     * block
     *
     * @param block the block
     * @return the placed block closed block instance
     */
    public abstract Optional<ClosedBlock> getFromBlock(final @NotNull Block block);

    /**
     * Add a block to the block storage
     *
     * @param block the block to add
     * @return if the block is added
     */
    public abstract boolean placeBlock(final ClosedBlock block);

    /**
     * Removes a block
     *
     * @param block the block to remove
     * @return if the block is removed
     */
    public abstract boolean destroyBlock(final ClosedBlock block);

    /**
     * Get the amount of blocks
     * in the store
     *
     * @return the amount of blocks
     */
    public abstract int size();

    /**
     * Get all the closed blocks
     *
     * @return the blocks
     */
    @NotNull
    public abstract Collection<ClosedBlock> getAllBlocks();

    /**
     * Get all the closed blocks matching the
     * specified type
     *
     * @param type the block type
     * @return the matching blocks
     */
    @NotNull
    public Collection<ClosedBlock> getAllBlocks(final @NotNull Class<? extends ClosedBlock> type) {
        return unmodifiable(this.getAllBlocks()
                .stream()
                .filter(byType(type))
                .collect(Collectors.toList()));
    }

    /**
     * Get all the closed blocks owned by
     * the specified player
     *
     * @param owner the blocks owner
     * @return the player blocks
     */
    @NotNull
    public Collection<ClosedBlock> getAllBlocks(final @NotNull OfflinePlayer owner) {
        return unmodifiable(this.getAllBlocks()
                .stream()
                .filter(byOwner(owner))
                .collect(Collectors.toList()));
    }

    /**
     * Get all the closed blocks in
     * the specified world
     *
     * @param world world the blocks world
     * @return the world blocks
     */
    public Collection<ClosedBlock> getAllBlocks(final @NotNull World world) {
        return unmodifiable(this.getAllBlocks()
                .stream()
                .filter(byWorld(world))
                .collect(Collectors.toList()));
    }

    /**
     * Get all the closed blocks owner
     * by the specified player and of the
     * specified type
     *
     * @param owner the blocks owner
     * @param type the block type
     * @return the player blocks of the
     * specified type
     */
    @NotNull
    public Collection<ClosedBlock> getAllBlocks(final @NotNull OfflinePlayer owner, final @NotNull Class<? extends ClosedBlock> type) {
        return unmodifiable(this.getAllBlocks()
                .stream()
                .filter(byType(type)
                        .and(byOwner(owner)))
                .collect(Collectors.toList()));
    }

    /**
     * Get all the closed blocks of the
     * specified type and in the specified
     * world
     *
     * @param world world the blocks world
     * @param type the block type
     * @return the player blocks of the
     * specified type
     */
    @NotNull
    public Collection<ClosedBlock> getAllBlocks(final @NotNull World world, final @NotNull Class<? extends ClosedBlock> type) {
        return unmodifiable(this.getAllBlocks()
                .stream()
                .filter(byType(type)
                        .and(byWorld(world)))
                .collect(Collectors.toList()));
    }

    /**
     * Get all the closed blocks owner
     * by the specified player and in
     * the specified world
     *
     * @param world world the blocks world
     * @param owner the blocks owner
     * @return the player blocks of the
     * specified type
     */
    @NotNull
    public Collection<ClosedBlock> getAllBlocks(final @NotNull World world, final @NotNull OfflinePlayer owner) {
        return unmodifiable(this.getAllBlocks()
                .stream()
                .filter(byOwner(owner)
                        .and(byWorld(world)))
                .collect(Collectors.toList()));
    }

    /**
     * Get all the closed blocks owner
     * by the specified player and of the
     * specified type in the provided
     * world
     *
     * @param world the blocks world
     * @param owner the blocks owner
     * @param type the block type
     * @return the player blocks of the
     * specified type
     */
    @NotNull
    public Collection<ClosedBlock> getAllBlocks(final @Nullable World world, final @Nullable OfflinePlayer owner, final @NotNull Class<? extends ClosedBlock> type) {
        return this.getAllBlocks().stream()
                .filter(byType(type)
                        .and(byOwner(owner))
                        .and(byWorld(world)))
                .collect(Collectors.toList());
    }

    private static Class<? extends ClosedBlock> getClosestType(final Class<? extends ClosedBlock> type) {
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> clazz : interfaces) {
            if (ClosedBlock.class.equals(clazz)) {
                return type;
            } else if (ClosedBlock.class.isAssignableFrom(clazz)) {
                return getClosestType(clazz.asSubclass(ClosedBlock.class));
            }
        }

        return type;
    }

    private static Predicate<ClosedBlock> byType(final Class<? extends ClosedBlock> type) {
        Class<? extends ClosedBlock> blockType = getClosestType(type);
        return (block -> blockType.isAssignableFrom(block.getClass()));
    }

    private static Predicate<ClosedBlock> byOwner(final OfflinePlayer player) {
        return (block ->  block.getOwner().equals(player));
    }

    private static Predicate<ClosedBlock> byWorld(final World world) {
        return (block ->  block.getWorld().equals(world));
    }

    private static <T> Collection<T> unmodifiable(final Collection<T> collection) {
        return Collections.unmodifiableCollection(collection);
    }
}