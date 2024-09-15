package com.github.karmadeb.closedblocks.api.block.data;

/**
 * Represents a block save data
 */
public interface SaveData {

    /**
     * Get if the block save data
     * exists
     *
     * @return if the block exists
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean exists();

    /**
     * Tries to save the block
     *
     * @return if the block was saved
     */
    boolean saveBlockData();

    /**
     * Tries to remove the block
     *
     * @return if the block was removed
     */
    boolean removeBlockData();
}
