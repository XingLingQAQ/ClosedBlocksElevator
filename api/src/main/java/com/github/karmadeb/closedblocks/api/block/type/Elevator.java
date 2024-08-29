package com.github.karmadeb.closedblocks.api.block.type;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents an elevator.
 */
public abstract class Elevator implements ClosedBlock {

    /**
     * Get the closed block type
     *
     * @return the block type
     */
    @Override
    public final BlockType<Elevator> getType() {
        return BlockType.ELEVATOR;
    }

    /**
     * Get if the elevator has a
     * previous floor
     *
     * @return if the elevator has previous
     * floor
     */
    public abstract boolean hasPrevious();

    /**
     * Get the previous elevator
     * floor
     *
     * @return the previous floor
     */
    @NotNull
    public abstract Optional<Elevator> getPrevious();

    /**
     * Get the elevator floor
     *
     * @return the current elevator floor
     */
    public abstract int getFloor();

    /**
     * Get the amount of floors
     * in this elevator
     *
     * @return the elevator floors
     */
    public abstract int getFloors();

    /**
     * Get the next elevator
     * floor
     *
     * @return the next floor
     */
    @NotNull
    public abstract Optional<Elevator> getNext();

    /**
     * Get if the elevator has a
     * next floor
     *
     * @return if the elevator has next floor
     */
    public abstract boolean hasNext();
}
