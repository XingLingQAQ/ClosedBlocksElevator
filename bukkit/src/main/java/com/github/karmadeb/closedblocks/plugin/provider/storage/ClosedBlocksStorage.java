package com.github.karmadeb.closedblocks.plugin.provider.storage;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.storage.BlockStorage;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClosedBlocksStorage extends BlockStorage {

    private final Lock lock = new ReentrantLock();
    private final List<ClosedBlock> blocks = new ArrayList<>();

    private int size = 0;

    public ClosedBlocksStorage() {}

    public void addAll(final Collection<? extends ClosedBlock> blocks) {
        this.blocks.addAll(blocks);
        this.size += blocks.size();
    }

    /**
     * Get if a block is a closed block
     * instance
     *
     * @param block the placed block
     * @return if the block is a closed block
     */
    @Override
    public boolean isClosedBlock(final @NotNull Block block) {
        return block.hasMetadata("closed_type");
    }

    /**
     * Get a closed block from a placed
     * block
     *
     * @param block the block
     * @return the placed block closed block instance
     */
    @Override
    public Optional<ClosedBlock> getFromBlock(final @NotNull Block block) {
        if (isClosedBlock(block))
            return getAllBlocks(block.getWorld())
                .stream().filter((cb) -> cb.getX() == block.getX() &&
                        cb.getY() == block.getY() && cb.getZ() == block.getZ())
                .findFirst();

        return Optional.empty();
    }

    /**
     * Add a block to the block storage
     *
     * @param block the block to add
     * @return if the block is added
     */
    @Override
    public boolean placeBlock(final ClosedBlock block) {
        lock.lock();
        try {
            if (block.getSaveData().saveBlockData()) {
                this.size++;
                return this.blocks.add(block);
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a block
     *
     * @param block the block to remove
     * @return if the block is removed
     */
    @Override
    public boolean destroyBlock(final ClosedBlock block) {
        lock.lock();
        try {
            if (block.getSaveData().removeBlockData()) {
                --this.size;
                return this.blocks.remove(block);
            }

            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the amount of blocks
     * in the store
     *
     * @return the amount of blocks
     */
    @Override
    public int size() {
        return this.size;
    }

    /**
     * Get all the closed blocks
     *
     * @return the blocks
     */
    @Override
    public @NotNull Collection<ClosedBlock> getAllBlocks() {
        return Collections.unmodifiableCollection(this.blocks);
    }
}
