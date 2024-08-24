package com.github.karmadeb.closedblocks.api.file.configuration.declaration;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.file.FileComponent;

/**
 * Represents a plugin file declaration
 */
public interface FileDeclaration<T> {

    /**
     * Get the file component
     *
     * @return the component
     */
    FileComponent getComponent();

    /**
     * Get the value path
     *
     * @return the path of the value
     */
    String getPath();

    /**
     * Get the default value
     *
     * @return the default value
     */
    T getDefault();

    /**
     * Get the configuration value
     *
     * @return the configuration value
     */
    default T get() {
        return ClosedAPI.getConfiguration(this);
    }

    /**
     * Get a casted value
     *
     * @param value the value
     * @return the cast value
     */
    T casted(final Object value);
}
