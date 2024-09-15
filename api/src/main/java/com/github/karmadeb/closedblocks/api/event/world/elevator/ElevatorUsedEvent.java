package com.github.karmadeb.closedblocks.api.event.world.elevator;

import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when a player
 * uses an elevator
 */
public class ElevatorUsedEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Elevator from;
    private final Elevator to;
    private final Direction direction;

    private boolean cancelled;

    /**
     * Create the event
     *
     * @param player the player who is related to this event
     * @param from the source elevator
     * @param to the target elevator
     */
    public ElevatorUsedEvent(final Player player, final Elevator from, final Elevator to) {
        this(player, from, to, (from.getY() > to.getY() ? Direction.DOWN : Direction.UP));
    }

    /**
     * Create the event
     *
     * @param player the player who is related to this event
     * @param from the source elevator
     * @param to the target elevator
     * @param direction the direction
     */
    public ElevatorUsedEvent(final Player player, final Elevator from, final Elevator to, final Direction direction) {
        super(player);
        this.from = from;
        this.to = to;
        this.direction = direction;
    }

    /**
     * Get the elevator the player
     * is going from
     *
     * @return the source elevator
     */
    public Elevator getFrom() {
        return this.from;
    }

    /**
     * Get the elevator the player
     * is going to
     *
     * @return the target elevator
     */
    public Elevator getTo() {
        return this.to;
    }

    /**
     * Get the direction the player is
     * going
     *
     * @return the direction
     */
    public Direction getDirection() {
        return this.direction;
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

    /**
     * Mine trigger reasons
     */
    public enum Direction {
        /**
         * Next floor
         */
        UP,
        /**
         * Previous floor
         */
        DOWN
    }
}
