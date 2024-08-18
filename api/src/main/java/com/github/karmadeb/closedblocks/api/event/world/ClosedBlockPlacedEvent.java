package com.github.karmadeb.closedblocks.api.event.world;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This event gets fired when a closed block
 * has been already placed
 */
public class ClosedBlockPlacedEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;
    private final ClosedBlock closedBlock;

    private boolean handled = false;

    /**
     * Create the event
     *
     * @param player the player who placed the block
     * @param block the block that has been placed at
     * @param closedBlock the closed block that is being
     *                    placed
     */
    public ClosedBlockPlacedEvent(final Player player, final Block block, final ClosedBlock closedBlock) {
        super(player);
        this.block = block;
        this.closedBlock = closedBlock;
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
    public ClosedBlock getPlacedBlock() {
        return this.closedBlock;
    }

    /**
     * Handles the event. When the event
     * is handled, it tells the bukkit integration
     * to not perform any action
     */
    public void handle() {
        this.handled = true;
    }

    /**
     * Get if the event has been
     * handled
     *
     * @return if the event has been
     * handled
     */
    public boolean isHandled() {
        return this.handled;
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
