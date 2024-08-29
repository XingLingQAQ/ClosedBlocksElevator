package com.github.karmadeb.closedblocks.api.block;

import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.data.SaveData;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a closed block
 */
public interface ClosedBlock {

    /**
     * Get the closed block type
     *
     * @return the block type
     */
    BlockType<? extends ClosedBlock> getType();

    /**
     * Get the closed block owner
     *
     * @return the owner of the block
     */
    OfflinePlayer getOwner();

    /**
     * Get the block world
     *
     * @return the world of the block
     */
    @NotNull
    World getWorld();

    /**
     * Get the block X position
     *
     * @return the X position
     */
    int getX();

    /**
     * Get the block Y position
     *
     * @return the Y position
     */
    int getY();

    /**
     * Get the block Z position
     *
     * @return the Z position
     */
    int getZ();

    /**
     * Get the closed block settings
     *
     * @return the block settings
     */
    BlockSettings getSettings();

    /**
     * Get the closed block save data
     *
     * @return the save data
     */
    SaveData getSaveData();
}
