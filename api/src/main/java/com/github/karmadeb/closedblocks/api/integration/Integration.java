package com.github.karmadeb.closedblocks.api.integration;

/**
 * Represents a ClosedBlock plugin integration. This
 * can be used by external plugins to enable a specific
 * integration, or by the own ClosedBlocks plugin to
 * provide official integrations with other plugins
 */
public interface Integration {

    /**
     * Get the integration name
     *
     * @return the name
     */
    String getName();

    /**
     * Load the integration
     */
    void load();

    /**
     * Unload the integration
     */
    void unload();

    /**
     * Get if the integration is supported
     *
     * @return if the integration is
     * supported by the plugin
     */
    boolean isSupported();
}
