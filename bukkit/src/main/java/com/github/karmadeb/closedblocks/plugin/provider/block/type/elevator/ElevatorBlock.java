package com.github.karmadeb.closedblocks.plugin.provider.block.type.elevator;

import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.data.SaveData;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.file.configuration.elevator.ElevatorConfig;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.provider.block.ClosedBlockSettings;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ElevatorBlock extends Elevator {

    private final OfflinePlayer owner;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final BlockSettings settings;
    private final SaveData saveData;

    private ElevatorBlock next;
    private int floor;

    /*
    Atomic integer is the best choice here. Why?
    Because then we will only need to update the
    value only from one elevator, to apply the value
    to all the other ones
     */
    private final AtomicInteger floors;
    private ElevatorBlock previous;

    public ElevatorBlock(final OfflinePlayer owner, final World world, final int x, final int y, final int z, final String disguise,
                         final AtomicInteger floors,
                         final ClosedBlocksPlugin plugin) {
        this(owner, world, x, y, z, floors, new ClosedBlockSettings(plugin, disguise), plugin);
    }

    public ElevatorBlock(final OfflinePlayer owner, final World world, final int x, final int y, final int z,
                         final AtomicInteger floors,
                         final ClosedBlockSettings settings,
                         final ClosedBlocksPlugin plugin) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        this.floors = floors;

        this.settings = settings;
        this.saveData = new ElevatorSaveData(plugin, this);
    }

    /**
     * Get if the elevator has a
     * previous level
     *
     * @return if the elevator has previous
     * level
     */
    @Override
    public boolean hasPrevious() {
        if (this.previous == null)
            return false;

        if (!this.previous.getSettings().isEnabled()) {
            Elevator previousElevator = this.previous.getPrevious().orElse(null);
            if (previousElevator == null)
                return false;

            return isWithinDistance(previousElevator, this);
        }

        return isWithinDistance(this.previous, this);
    }

    /**
     * Get the previous elevator
     * level
     *
     * @return the previous level
     */
    @Override
    public @NotNull Optional<Elevator> getPrevious() {
        if (this.previous == null)
            return Optional.empty();

        if (!this.previous.getSettings().isEnabled()) {
            Elevator previousElevator = this.previous.getPrevious().orElse(null);
            if (isWithinDistance(previousElevator, this))
                return Optional.of(previousElevator);

            return Optional.empty();
        }

        if (isWithinDistance(this.previous, this))
            return Optional.of(this.previous);

        return Optional.empty();
    }

    public void setPrevious(final ElevatorBlock previous) {
        this.previous = previous;
    }

    /**
     * Get the elevator level
     *
     * @return the current elevator level
     */
    @Override
    public int getFloor() {
        return this.floor;
    }

    public void setFloor(final int floor) {
        this.floor = floor;
    }

    public AtomicInteger getFloorAtomic() {
        return this.floors;
    }

    /**
     * Get the amount of floors
     * in this elevator
     *
     * @return the elevator floors
     */
    @Override
    public int getFloors() {
        return this.floors.get();
    }

    /**
     * Get the next elevator
     * level
     *
     * @return the next level
     */
    @Override
    public @NotNull Optional<Elevator> getNext() {
        if (this.next == null)
            return Optional.empty();

        if (!this.next.getSettings().isEnabled()) {
            Elevator nextElevator = this.next.getNext().orElse(null);
            if (isWithinDistance(this, nextElevator))
                return Optional.of(nextElevator);

            return Optional.empty();
        }

        if (isWithinDistance(this, this.next))
            return Optional.of(this.next);

        return Optional.empty();
    }

    public void setNext(final ElevatorBlock next) {
        this.next = next;
    }

    /**
     * Get if the elevator has a
     * next level
     *
     * @return if the elevator has next level
     */
    @Override
    public boolean hasNext() {
        if (this.next == null)
            return false;

        if (!this.next.getSettings().isEnabled()) {
            Elevator nextElevator = this.next.getNext().orElse(null);
            if (nextElevator == null)
                return false;

            return isWithinDistance(this, nextElevator);
        }

        return isWithinDistance(this, next);
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

    private static boolean isWithinDistance(final Elevator from, final Elevator to) {
        if (to == null || from == null)
            return false;

        int distance = Math.abs(to.getY()) - Math.abs(from.getY());
        return distance <= ElevatorConfig.MAX_DISTANCE.get().intValue();
    }
}
