package com.github.karmadeb.closedblocks.api.file.messages.elevator;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;

/**
 * Represents an elevator message
 */
public enum ElevatorMessage implements MessageDeclaration {
    PLACEMENT_SUCCESS("PlacementSuccess", "&3Successfully placed elevator"),
    PLACEMENT_FAILED("PlacementFailed", "&dFailed to place elevator"),
    PLACEMENT_TOO_CLOSE("PlacementTooClose", "&dThe elevator is too close to another elevator"),
    PLACEMENT_TOO_FAR("PlacementTooFar", "&eThe elevator is too far from another elevator"),
    DISGUISE_SUCCESS("DisguiseSuccess", "&3Successfully disguised elevator"),
    DISGUISE_FAILED("DisguiseFailed", "&dFailed to disguise elevator"),
    DISGUISE_FAILED_TYPE("DisguisedFailedType", "&dFailed to disguise elevator. Disguise type is not allowed"),
    DISGUISE_NOT_ALLOWED("DisguiseNotAllowed", "&dYou are not allowed to disguise that elevator"),
    DESTROY_SUCCESS("DestroySuccess", "&3Successfully removed elevator"),
    DESTROY_FAILED("DestroyFailed", "&dFailed to remove elevator"),
    DESTROY_INVENTORY_FULL("DestroyInventoryFull", "&dFailed to remove elevator. Your inventory is full"),
    DESTROY_NOT_ALLOWED("DestroyNotAllowed", "&dYou are not allowed to destroy that elevator"),
    FLOOR_UP_TITLE("FloorUpTitle", "&3↟ &bFloor&3 ↟"),
    FLOOR_UP_SUBTITLE("FloorUpSubtitle", "&7{floor}"),
    FLOOR_DOWN_TITLE("FloorDownTitle", "&3↡ &bFloor&3 ↡"),
    FLOOR_DOWN_SUBTITLE("FloorDownSubtitle", "&7{floor}"),
    VIEW_ADD_NAME("ViewerAddName", "&bWrite the name of the player you want to allow to view the elevator. Or type&c .cancel&b to cancel"),
    VIEW_REMOVE_NAME("ViewerRemoveName", "&bWrite the name of the player you want to deny to view the elevator. Or type&c .cancel&b to cancel"),
    VIEW_ADD_SUCCESS("ViewAddSuccess", "&3Successfully added &7{player}"),
    VIEW_ADD_FAILED("ViewAddFailed", "&dFailed to add&7 {player}"),
    VIEW_REMOVE_SUCCESS("ViewRemoveSuccess", "&3Successfully removed &7{player}"),
    VIEW_REMOVE_FAILED("ViewRemoveFailed", "&dFailed to remove&7 {player}");

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
