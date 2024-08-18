package com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events;

import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.ItemsAdderIntegration;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderListener implements Listener {

    private final ItemsAdderIntegration integration;

    public ItemsAdderListener(final ItemsAdderIntegration integration) {
        this.integration = integration;
    }

    @EventHandler
    public void onDataLoaded(final ItemsAdderLoadDataEvent e) {
        if (!e.getCause().equals(ItemsAdderLoadDataEvent.Cause.FIRST_LOAD))
            return;

        integration.loadStep2();
    }
}
