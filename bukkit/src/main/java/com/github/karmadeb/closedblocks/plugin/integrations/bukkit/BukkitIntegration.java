package com.github.karmadeb.closedblocks.plugin.integrations.bukkit;

import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events.BBlockPlaceRemoveListener;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events.BClosedBlockPlacedListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

@SuppressWarnings("SameParameterValue")
public class BukkitIntegration implements Integration {

    private final ClosedBlocksPlugin plugin;

    private BBlockPlaceRemoveListener blockPlaceOrRemoveListener;
    private BClosedBlockPlacedListener apiBlockPlacedListener;

    public BukkitIntegration(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public ClosedBlocksPlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Get the integration name
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "ClosedBlocks bukkit";
    }

    /**
     * Load the integration
     */
    @Override
    public void load() {
        blockPlaceOrRemoveListener = new BBlockPlaceRemoveListener(this);
        apiBlockPlacedListener = new BClosedBlockPlacedListener();

        Bukkit.getPluginManager().registerEvents(blockPlaceOrRemoveListener, plugin);
        Bukkit.getPluginManager().registerEvents(apiBlockPlacedListener, plugin);
    }

    /**
     * Unload the integration
     */
    @Override
    public void unload() {
        HandlerList.unregisterAll(blockPlaceOrRemoveListener);
        HandlerList.unregisterAll(apiBlockPlacedListener);
    }

    /**
     * Get if the integration is supported
     *
     * @return if the integration is
     * supported by the plugin
     */
    @Override
    public boolean isSupported() {
        return true;
    }
}
