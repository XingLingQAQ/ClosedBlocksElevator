package com.github.karmadeb.closedblocks.api.event.world.mine;

import com.github.karmadeb.closedblocks.api.block.type.Mine;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This event gets called whenever a mine is triggered
 */
public class MineTriggeredEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Entity entity;
    private final Mine mine;
    private final Reason reason;
    private final MineTriggeredEvent chained;

    /**
     * Create the event
     *
     * @param entity the entity who is related to this event
     * @param mine the mine that has been triggered
     * @param reason the mine trigger reason
     * @param chained the trigger chained event
     */
    public MineTriggeredEvent(final Entity entity, final Mine mine, final Reason reason, final MineTriggeredEvent chained) {
        this.entity = entity;
        this.mine = mine;
        this.reason = reason;
        this.chained = chained;
    }

    /**
     * Get the entity which triggered
     * the mine
     *
     * @return the mine trigger
     */
    public Entity getEntity() {
        return this.entity;
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
     * Get the mine trigger reason
     *
     * @return the reason of the mine trigger
     */
    public Reason getReason() {
        return this.reason;
    }

    /**
     * Get if the event is from a chain
     * of events
     *
     * @return if the event is from a chain
     */
    public boolean isChained() {
        return this.chained != null;
    }

    /**
     * Get the chained event
     *
     * @return the chained event
     */
    public MineTriggeredEvent getChained() {
        return this.chained;
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
    public enum Reason {
        /**
         * Player stepped on
         */
        STEP("Entity step"),
        /**
         * Player broke block
         */
        BREAK("Entity block break"),
        /**
         * Entity pickups the mine
         */
        PICKUP("Entity block pickup"),
        /**
         * Piston moved the mine
         */
        PISTON("Piston movement"),
        /**
         * Another explosion
         */
        CHAIN_REACTION("Chained reaction"),
        /**
         * Fire
         */
        FIRE("Fire");

        private final String name;

        Reason(final String name) {
            this.name = name;
        }

        /**
         * Get the pretty name
         *
         * @return the reason pretty name
         */
        public String getName() {
            return this.name;
        }
    }
}
