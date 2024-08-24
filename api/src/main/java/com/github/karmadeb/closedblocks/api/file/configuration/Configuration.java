package com.github.karmadeb.closedblocks.api.file.configuration;

import com.github.karmadeb.closedblocks.api.file.configuration.declaration.FileDeclaration;

/**
 * Represents the plugin configuration
 */
public interface Configuration {

    /**
     * Reloads the messages
     */
    void reload();

    /**
     * Get a value
     *
     * @param declaration the value declaration
     * @return the value
     */
    <T> T getValue(final FileDeclaration<T> declaration);
}
