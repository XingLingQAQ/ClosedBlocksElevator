package com.github.karmadeb.closedblocks.plugin.provider.block;

import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.SaveData;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
public class ElevatorBlock implements Elevator {

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
        this(owner, world, x, y, z, floors, new ClosedBlockSettings(disguise), plugin);
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
        this.saveData = new BlockSaveData(plugin, this);
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
        return this.previous != null;
    }

    /**
     * Get the previous elevator
     * level
     *
     * @return the previous level
     */
    @Override
    public @NotNull Optional<Elevator> getPrevious() {
        return Optional.ofNullable(this.previous);
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

    public void setFloors(final int floors) {
        this.floors.set(floors);
    }

    /**
     * Get the next elevator
     * level
     *
     * @return the next level
     */
    @Override
    public @NotNull Optional<Elevator> getNext() {
        return Optional.ofNullable(this.next);
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
        return this.next != null;
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
