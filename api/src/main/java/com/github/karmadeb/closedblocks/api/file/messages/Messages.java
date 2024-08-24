package com.github.karmadeb.closedblocks.api.file.messages;

import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;

/**
 * Represents a messages holder
 * of the plugin
 */
public interface Messages {

    /**
     * Reloads the messages
     */
    void reload();

    /**
     * Get a message
     *
     * @param declaration the message declaration
     * @param parameters the message parameters
     * @return the message
     */
    String getMessage(final MessageDeclaration declaration, final MessageParameter... parameters);
}
