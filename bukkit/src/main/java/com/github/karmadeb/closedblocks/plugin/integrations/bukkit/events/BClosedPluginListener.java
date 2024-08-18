package com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events;

import com.github.karmadeb.closedblocks.api.event.plugin.ClosedPluginStateChangedEvent;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.BukkitIntegration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class BClosedPluginListener implements Listener {

    private final BukkitIntegration integration;

    public BClosedPluginListener(final BukkitIntegration integration) {
        this.integration = integration;
    }

    @EventHandler
    public void onReload(ClosedPluginStateChangedEvent e) {
        if (!e.getState().equals(ClosedPluginStateChangedEvent.State.RELOAD))
            return;

        integration.reloadRecipes();
    }
}
