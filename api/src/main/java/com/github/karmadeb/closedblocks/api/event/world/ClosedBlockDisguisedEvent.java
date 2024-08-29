package com.github.karmadeb.closedblocks.api.event.world;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event gets fired when a closed block
 * has been disguised
 */
public class ClosedBlockDisguisedEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;
    private final ClosedBlock closedBlock;
    private final String previousDisguise;
    private final String previousDisguiseName;

    private boolean cancelled = false;

    /**
     * Create the event
     *
     * @param player the player who placed the block
     * @param block the block that has been placed at
     * @param closedBlock the closed block that is being
     *                    placed
     * @param previousDisguise the previous disguise
     * @param previousDisguiseName the previous disguise name
     */
    public ClosedBlockDisguisedEvent(final Player player, final Block block, final ClosedBlock closedBlock,
                                     final String previousDisguise, final String previousDisguiseName) {
        super(player);
        this.block = block;
        this.closedBlock = closedBlock;
        this.previousDisguise = previousDisguise;
        this.previousDisguiseName = previousDisguiseName;
    }

    /**
     * Get the bukkit block of
     * the closed block
     *
     * @return the block
     */
    public Block getBlock() {
        return this.block;
    }

    /**
     * Get the closed block that has
     * been placed
     *
     * @return the placed closed block
     */
    public ClosedBlock getDisguisedBlock() {
        return this.closedBlock;
    }

    /**
     * Get the previous disguise
     *
     * @return the previous disguise
     */
    public String getPreviousDisguise() {
        return this.previousDisguise;
    }

    /**
     * Get the previous disguise name
     *
     * @return the previous disguise name
     */
    public String getPreviousDisguiseName() {
        return this.previousDisguiseName;
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
        cancelled = cancel;
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
