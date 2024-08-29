package com.github.karmadeb.closedblocks.api.file.messages.mine;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;

/**
 * Represents a mine message
 */
public enum MineMessage implements MessageDeclaration {
    POWER_INCREASED("PowerIncreased", "&3Mine power increased"),
    POWER_CANNOT_INCREASE("PowerCannotIncrease", "&dMine power cannot increase any more!"),
    MINE_INCENDIARY("MineIncendiary", "&3Mine will now cause fire"),
    ALREADY_INCENDIARY("MineAlreadyIncendiary", "&dMine is already incendiary"),
    MINE_FUSED("MineFused", "&3Mine is now active again");

    private final String path;
    private final String defaultMessage;

    MineMessage(final String path, final String defaultMessage) {
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
