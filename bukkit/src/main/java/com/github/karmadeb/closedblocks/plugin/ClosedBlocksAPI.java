package com.github.karmadeb.closedblocks.plugin;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.file.configuration.Configuration;
import com.github.karmadeb.closedblocks.api.file.messages.Messages;
import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.plugin.provider.file.ConfigurationFile;
import com.github.karmadeb.closedblocks.plugin.provider.file.MessagesFile;
import com.github.karmadeb.closedblocks.plugin.provider.storage.ClosedBlocksStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClosedBlocksAPI extends ClosedAPI {

    private final ClosedBlocksStorage storage = new ClosedBlocksStorage();
    private final Set<Integration> integrations = ConcurrentHashMap.newKeySet();

    private final ClosedBlocksPlugin plugin;
    private final Messages messages;
    private final Configuration configuration;

    public ClosedBlocksAPI(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
        this.messages = new MessagesFile(plugin);
        this.configuration = new ConfigurationFile(plugin);
    }

    public void register() {
        this.setAsInstance();
    }

    public void shutdown() {
        integrations.forEach(this::removeIntegration);
    }

    /**
     * Get the plugin block storage
     *
     * @return the block storage
     */
    @Override
    public ClosedBlocksStorage getBlockStorage() {
        return this.storage;
    }

    /**
     * Adds an integration to the API
     *
     * @param integration the integration to add
     */
    @Override
    public void addIntegration(final @NotNull Integration integration) {
        if (!integration.isSupported()) {
            plugin.getLogger().warning("Not loading integration " + integration.getName() + " because it is not supported");
            return;
        }

        if (!integrations.add(integration))
            return;

        plugin.getLogger().info("Loading " + integration.getName() + " integration");
        integration.load();
    }

    /**
     * Removes an integration from the
     * API
     *
     * @param integration the integration to remove
     */
    @Override
    public void removeIntegration(final @NotNull Integration integration) {
        if (integrations.remove(integration)) {
            plugin.getLogger().info("Unloading ClosedBlock integration " + integration.getName());
            integration.unload();
        }
    }

    /**
     * Get the plugin messages
     *
     * @return the plugin messages
     */
    @Override
    public Messages getMessages() {
        return this.messages;
    }

    /**
     * Get the plugin configuration
     *
     * @return the plugin configuration
     */
    @Override
    public Configuration getConfig() {
        return this.configuration;
    }
}