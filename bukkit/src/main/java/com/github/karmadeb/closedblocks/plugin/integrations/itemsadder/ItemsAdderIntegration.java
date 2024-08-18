package com.github.karmadeb.closedblocks.plugin.integrations.itemsadder;

import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events.ItemsAdderListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class ItemsAdderIntegration implements Integration {

    private final ClosedBlocksPlugin plugin;
    private ItemsAdderListener pluginLoadListener;

    public ItemsAdderIntegration(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the integration name
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "ItemsAdder";
    }

    /**
     * Load the integration
     */
    @Override
    public void load() {
        pluginLoadListener = new ItemsAdderListener(this);
        plugin.getServer().getPluginManager().registerEvents(pluginLoadListener, plugin);
    }

    public void loadStep2() {

    }

    /**
     * Unload the integration
     */
    @Override
    public void unload() {
        if (pluginLoadListener != null)
            HandlerList.unregisterAll(pluginLoadListener);
    }

    /**
     * Get if the integration is supported
     *
     * @return if the integration is
     * supported by the plugin
     */
    @Override
    public boolean isSupported() {
        return Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }
}
