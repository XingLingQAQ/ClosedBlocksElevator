package com.github.karmadeb.closedblocks.api.event.plugin;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event is fired when the plugin
 * changes its state
 */
public class ClosedPluginStateChangedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final State state;

    /**
     * Initialize the event
     *
     * @param state the plugin state
     */
    public ClosedPluginStateChangedEvent(final State state) {
        this.state = state;
    }

    /**
     * Get the plugin state
     *
     * @return the plugin state
     */
    public State getState() {
        return this.state;
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
     * Defines the closed plugin states
     */
    public enum State {
        START,
        RELOAD,
        STOP
    }
}
