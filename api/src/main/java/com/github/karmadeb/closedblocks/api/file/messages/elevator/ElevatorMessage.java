package com.github.karmadeb.closedblocks.api.file.messages.elevator;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;

/**
 * Represents an elevator message
 */
public enum ElevatorMessage implements MessageDeclaration {
    FLOOR_UP_TITLE("FloorUpTitle", "&3↟ &bFloor&3 ↟"),
    FLOOR_UP_SUBTITLE("FloorUpSubtitle", "&7{floor}"),
    FLOOR_DOWN_TITLE("FloorDownTitle", "&3↡ &bFloor&3 ↡"),
    FLOOR_DOWN_SUBTITLE("FloorDownSubtitle", "&7{floor}");

    private final String path;
    private final String defaultMessage;

    ElevatorMessage(final String path, final String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    /**
     * Get the message component
     *
     * @return the component
     */
    @Override
    public FileComponent getComponent() {
        return FileComponent.ELEVATOR;
    }

    /**
     * Get the message path
     *
     * @return the path of the message
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Get the default message
     *
     * @return the default message
     */
    @Override
    public String getDefault() {
        return this.defaultMessage;
    }
}
