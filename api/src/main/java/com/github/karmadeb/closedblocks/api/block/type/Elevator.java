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
     * previous level
     *
     * @return if the elevator has previous
     * level
     */
    boolean hasPrevious();

    /**
     * Get the previous elevator
     * level
     *
     * @return the previous level
     */
    @NotNull
    Optional<Elevator> getPrevious();

    /**
     * Get the elevator level
     *
     * @return the current elevator level
     */
    int getLevel();

    /**
     * Get the next elevator
     * level
     *
     * @return the next level
     */
    @NotNull
    Optional<Elevator> getNext();

    /**
     * Get if the elevator has a
     * next level
     *
     * @return if the elevator has next level
     */
    boolean hasNext();
}
