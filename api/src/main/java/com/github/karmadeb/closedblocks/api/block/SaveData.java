package com.github.karmadeb.closedblocks.api.block;

/**
 * Represents a block save data
 */
public interface SaveData {

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
