package com.github.karmadeb.closedblocks.plugin.provider.block.type.mine;

import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.data.SaveData;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.provider.block.ClosedBlockSettings;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class MineBlock extends Mine {

    private final OfflinePlayer owner;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final BlockSettings settings;
    private final SaveData saveData;

    private float power;
    private boolean fire;
    private boolean defused;

    public MineBlock(final OfflinePlayer owner, final World world, final int x, final int y, final int z, final String disguise,
                     final float power, final boolean fire, final boolean defused, final ClosedBlocksPlugin plugin) {
        this(owner, world, x, y, z, power, fire, defused, new ClosedBlockSettings(plugin, disguise), plugin);
    }

    public MineBlock(final OfflinePlayer owner, final World world, final int x, final int y, final int z,
                     final float power, final boolean fire, final boolean defused, final BlockSettings settings, final ClosedBlocksPlugin plugin) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.settings = settings;
        this.saveData = new MineSaveData(plugin, this);

        this.power = power;
        this.fire = fire;
        this.defused = defused;
    }

    /**
     * Get the power of the mine
     *
     * @return the mine power
     */
    @Override
    public float getPower() {
        return this.power;
    }

    /**
     * Set the mine power
     *
     * @param power the new mine power
     */
    @Override
    public void setPower(final float power) {
        this.power = Math.max(1, power);
    }

    /**
     * Get if the mine causes fire upon
     * explosion
     *
     * @return if the mine causes fire
     * when it explodes
     */
    @Override
    public boolean causesFire() {
        return this.fire;
    }

    /**
     * Set if the mine causes fire when
     * it explodes
     *
     * @param causesFire if the mine causes
     *                   fire
     */
    @Override
    public void setCausesFire(final boolean causesFire) {
        this.fire = causesFire;
    }

    /**
     * Get if the mine is defused
     *
     * @return if the mine is defused
     */
    @Override
    public boolean isDefused() {
        return this.defused;
    }

    /**
     * Set if the mine is defused
     *
     * @param status the mine defuse status
     */
    @Override
    public void setDefused(final boolean status) {
        this.defused = status;
    }

    /**
     * Get the closed block owner
     *
     * @return the owner of the block
     */
    @Override
    public OfflinePlayer getOwner() {
        return this.owner;
    }

    /**
     * Get the block world
     *
     * @return the world of the block
     */
    @Override
    public @NotNull World getWorld() {
        return this.world;
    }

    /**
     * Get the block X position
     *
     * @return the X position
     */
    @Override
    public int getX() {
        return this.x;
    }

    /**
     * Get the block Y position
     *
     * @return the Y position
     */
    @Override
    public int getY() {
        return this.y;
    }

    /**
     * Get the block Z position
     *
     * @return the Z position
     */
    @Override
    public int getZ() {
        return this.z;
    }

    /**
     * Get the closed block settings
     *
     * @return the block settings
     */
    @Override
    public BlockSettings getSettings() {
        return this.settings;
    }

    /**
     * Get the closed block save data
     *
     * @return the save data
     */
    @Override
    public SaveData getSaveData() {
        return this.saveData;
    }
}
