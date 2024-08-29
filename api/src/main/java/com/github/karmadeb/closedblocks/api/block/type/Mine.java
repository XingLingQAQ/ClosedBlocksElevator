package com.github.karmadeb.closedblocks.api.block.type;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;

/**
 * Represents an explosive mine
 */
public abstract class Mine implements ClosedBlock {

    /**
     * Get the closed block type
     *
     * @return the block type
     */
    @Override
    public final BlockType<Mine> getType() {
        return BlockType.MINE;
    }

    /**
     * Get the power of the mine
     *
     * @return the mine power
     */
    public abstract float getPower();

    /**
     * Set the mine power
     *
     * @param power the new mine power
     */
    public abstract void setPower(final float power);

    /**
     * Get if the mine causes fire upon
     * explosion
     *
     * @return if the mine causes fire
     * when it explodes
     */
    public abstract boolean causesFire();

    /**
     * Set if the mine causes fire when
     * it explodes
     *
     * @param causesFire if the mine causes
     *                   fire
     */
    public abstract void setCausesFire(final boolean causesFire);

    /**
     * Get if the mine is defused
     *
     * @return if the mine is defused
     */
    public abstract boolean isDefused();

    /**
     * Set if the mine is defused
     *
     * @param status the mine defuse status
     */
    public abstract void setDefused(final boolean status);
}