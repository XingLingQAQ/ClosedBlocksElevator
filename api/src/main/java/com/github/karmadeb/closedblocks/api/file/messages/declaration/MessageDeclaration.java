package com.github.karmadeb.closedblocks.api.file.messages.declaration;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.file.FileComponent;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a plugin message declaration
 */
public interface MessageDeclaration {

    /**
     * Get the file component
     *
     * @return the component
     */
    FileComponent getComponent();

    /**
     * Get the message path
     *
     * @return the path of the message
     */
    String getPath();

    /**
     * Get the default message
     *
     * @return the default message
     */
    String getDefault();

    /**
     * Parse the message using the current
     * ClosedAPI
     *
     * @param parameters the message parameters
     * @return the parsed message
     */
    default String parse(final MessageParameter... parameters) {
        return ClosedAPI.getMessage(this, parameters);
    }

    /**
     * Send the message to the sender
     *
     * @param sender the sender
     * @param parameters the message parameters
     */
    default void send(final @NotNull CommandSender sender, final MessageParameter... parameters) {
        String parsed = parse(parameters);
        sender.sendMessage(parsed);
    }
}
