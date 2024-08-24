package com.github.karmadeb.closedblocks.api.block.type;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Represents an elevator.
 */
public interface Elevator extends ClosedBlock {

    /**
     * Get if the elevator has a
     * previous floor
     *
     * @return if the elevator has previous
     * floor
     */
    boolean hasPrevious();

    /**
     * Get the previous elevator
     * floor
     *
     * @return the previous floor
     */
    @NotNull
    Optional<Elevator> getPrevious();

    /**
     * Get the elevator floor
     *
     * @return the current elevator floor
     */
    int getFloor();

    /**
     * Get the amount of floors
     * in this elevator
     *
     * @return the elevator floors
     */
    int getFloors();

    /**
     * Get the next elevator
     * floor
     *
     * @return the next floor
     */
    @NotNull
    Optional<Elevator> getNext();

    /**
     * Get if the elevator has a
     * next floor
     *
     * @return if the elevator has next floor
     */
    boolean hasNext();
}
