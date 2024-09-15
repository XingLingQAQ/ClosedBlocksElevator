package com.github.karmadeb.closedblocks.api.event.world.mine;

import com.github.karmadeb.closedblocks.api.block.type.Mine;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event gets called whenever a mine is defused
 */
public class MineDefusedEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Mine mine;

    private boolean cancelled;

    /**
     * Create the event
     *
     * @param player the player who is related to this event
     * @param mine the mine that has been defused
     */
    public MineDefusedEvent(final Player player, final Mine mine) {
        super(player);
        this.mine = mine;
    }

    /**
     * Get the closed block that has
     * been placed
     *
     * @return the triggered mine
     */
    public Mine getMine() {
        return this.mine;
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
