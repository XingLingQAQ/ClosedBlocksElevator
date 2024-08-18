package com.github.karmadeb.closedblocks.api;

import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.api.storage.BlockStorage;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the ClosedBlocks API
 */
public abstract class ClosedAPI {

    private static ClosedAPI instance;

    /**
     * Define the ClosedAPI instance
     * to the current
     */
    protected void setAsInstance() {
        if (instance != null)
            throw new IllegalStateException("Cannot define ClosedAPI because it has been already defined");

        instance = this;
    }

    /**
     * Get the plugin block storage
     *
     * @return the block storage
     */
    public abstract BlockStorage getBlockStorage();

    /**
     * Adds an integration to the API
     *
     * @param integration the integration to add
     */
    public abstract void addIntegration(final @NotNull Integration integration);

    /**
     * Removes an integration from the
     * API
     *
     * @param integration the integration to remove
     */
    public abstract void removeIntegration(final @NotNull Integration integration);

    /**
     * Get the API instance
     *
     * @return the API instance
     */
    public static ClosedAPI getInstance() {
        return instance;
    }
}
