package com.github.karmadeb.closedblocks.api.event.world;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event gets fired before the actual
 * {@link com.github.karmadeb.closedblocks.api.block.ClosedBlock} is placed, and this event exists
 * in order to allow other plugins to define when
 * a ClosedBlock can be placed or not
 */
public class ClosedBlockPreparePlaceEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;

    private boolean cancelled = false;

    /**
     * Create the event
     *
     * @param player the player that wants to put
     *               the block
     * @param block the block that the player
     *              wants to build on
     */
    public ClosedBlockPreparePlaceEvent(final Player player, final Block block) {
        super(player);
        this.block = block;
    }

    /**
     * Get the block where the closed
     * block is being placed
     *
     * @return the block where closed block
     * is being placed
     */
    public Block getBlock() {
        return this.block;
    }

    /**
     * Gets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins
     *
     * @return true if this event is cancelled
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Sets the cancellation state of this event. A cancelled event will not
     * be executed in the server, but will still pass to other plugins.
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * Get the event handler list
     *
     * @return the event handler list
     */
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    /**
     * Get the event handler list
     *
     * @return the event handler list
     */
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
