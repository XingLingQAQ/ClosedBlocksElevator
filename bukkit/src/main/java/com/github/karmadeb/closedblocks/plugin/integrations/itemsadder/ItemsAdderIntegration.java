package com.github.karmadeb.closedblocks.plugin.integrations.itemsadder;

import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events.IABlockPlaceRemoveListener;
import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events.IAClosedBlockPlacedListener;
import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events.ItemsAdderListener;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class ItemsAdderIntegration implements Integration {

    private final ClosedBlocksPlugin plugin;
    private final Runnable onStepTwo;

    private ItemsAdderListener pluginLoadListener;
    private IABlockPlaceRemoveListener blockPlaceOrRemoveListener;
    private IAClosedBlockPlacedListener apiBlockPlacedListener;

    public ItemsAdderIntegration(final ClosedBlocksPlugin plugin, final Runnable onStepTwo) {
        this.plugin = plugin;
        this.onStepTwo = onStepTwo;
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
        return "ItemsAdder";
    }

    /**
     * Load the integration
     */
    @Override
    public void load() {
        pluginLoadListener = new ItemsAdderListener(this);
        blockPlaceOrRemoveListener = new IABlockPlaceRemoveListener(this);
        apiBlockPlacedListener = new IAClosedBlockPlacedListener();

        plugin.getServer().getPluginManager().registerEvents(pluginLoadListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(blockPlaceOrRemoveListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(apiBlockPlacedListener, plugin);
    }

    public void loadStep2() {
        this.onStepTwo.run();
    }

    /**
     * Unload the integration
     */
    @Override
    public void unload() {
        if (pluginLoadListener != null)
            HandlerList.unregisterAll(pluginLoadListener);

        if (blockPlaceOrRemoveListener != null)
            HandlerList.unregisterAll(blockPlaceOrRemoveListener);

        if (apiBlockPlacedListener != null)
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
        return Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }

    public boolean has(final String namespace) {
        if (isSupported())
            return dev.lone.itemsadder.api.CustomBlock.isInRegistry(namespace);

        return false;
    }
}
