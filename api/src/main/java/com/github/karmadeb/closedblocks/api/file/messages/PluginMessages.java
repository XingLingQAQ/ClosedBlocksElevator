package com.github.karmadeb.closedblocks.api.file.messages;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;

/**
 * Represents a plugin message
 */
public enum PluginMessages implements MessageDeclaration {
    PERMISSION("Permission", "&dYou don't have the permission &7{permission}"),
    PLAYER_REQUIRED("PlayerRequired", "&dThis command is not intended for CONSOLE usage"),
    INVALID_ACTION("InvalidAction", "&dInvalid action. Valid actions are&7: {actions}"),
    INCOMPLETE_ACTION("IncompleteAction", "&dInvalid or incomplete action. Run &8\"&7{label} help {action}&8\"&d for help"),
    HELP_TITLE("HelpTitle", "&8&m------&3 ClosedBlocks &8&m------"),
    HELP_INLINE_GIVE("HelpInlineGive", "&8/&7{label} give &8- &fGives a closed block to a player"),
    HELP_INLINE_RELOAD("HelpInlineReload", "&8/&7{label} reload &8- &fReloads the plugin files and recipes"),
    HELP_INLINE_MANAGE("HelpInlineManage", "&8/&7{label} manage &8- &fManages all the owned closed blocks"),
    HELP_GIVE("HelpGive", new String[]{
            "&7Gives the&d [player]&7 a closed block",
            "&dParameters:",
            "  &8-&7 <&3block type&7> &8- &fThe closed block type",
            "  &8-&7 <&3player&7>     &8- &fThe player to give the elevator",
            "  &8-&7 [&3amount&7]     &8- &fThe amount of blocks to give",
            "&dPermissions:",
            "  &8- &7closedblocks.give",
            "  &8- &7closedblocks.give.&d<&3block type&d>",
    }),
    HELP_RELOAD("HelpReload", new String[]{
            "&7Reloads the plugin files and recipes",
            "&dPermissions:",
            "  &8- &7closedblocks.reload"
    }),
    HELP_MANAGE("HelpManage", new String[]{
            "&7Opens a menu to manage all the placed closed blocks",
            "&dParameters:",
            "  &8-&7 [&3name&7] &8- &fThe closed block name"
    }),
    RENAME_TYPE_CHAT("RenameTypeInChat", "&3Type in chat the new block name, or type&d .cancel&3 to cancel"),
    BLOCK_MANAGE_REMOVED("BlockManageRemoved", "&dThe block you were managing has been removed"),
    RELOAD_SUCCESS("ReloadSuccess", "&3Successfully reloaded ClosedBlocks files and recipes"),
    RELOAD_FAILED("ReloadFailed", "&dFailed to reload ClosedBlocks files and recipes"),
    GIVE_UNKNOWN_BLOCK("GiveUnknownBlock", "&dFailed to give closed block to&7 {player}&d. Unknown type:&7 {type}"),
    GIVE_PLAYER_OFFLINE("GivePlayerOffline", "&dFailed to give closed block to&7 {player}&d. Player is offline"),
    GIVE_INVALID_AMOUNT("GiveInvalidAmount", "&dFailed to give closed block to&7 {player}&d. Specified amount is invalid"),
    GIVE_INVENTORY_FULL("GiveInventoryFull", "&dFailed to give closed block to&7 {player}&d. Target inventory only has &7{slots}&d available slots"),
    GIVE_SUCCESS("GiveSuccess", "&3Successfully give&7 {amount}&3 blocks of &7{type}&3 to&7 {player}"),
    GIVE_RECEIVED("GiveReceived", "&3You've received &7{amount}&3 blocks of &7{type}&3 from&7 {player}"),
    BLOCK_MANAGER_NEXT("BlockManagerNext", "&7Next"),
    BLOCK_MANAGER_PREVIOUS("BlockManagerPrevious", "&7Previous"),
    BLOCK_MANAGER_CANCEL("BlockManagerCancel", "&cCancel"),
    BLOCK_MANAGER_NAME("BlockManagerName", "&7Name: &3{name}"),
    BLOCK_MANAGER_TYPE("BlockManagerType", "&7Type: &3{type}"),
    BLOCK_MANAGER_WORLD("BlockManagerWorld", "&7World: &3{world}"),
    BLOCK_MANAGER_COORDS("BlockManagerCoords", "&7Coords: &8X&7: &3{x} &8Y&7: &3{x} &8Z&7: &3{x}"),
    BLOCK_MANAGER_DISGUISE("BlockManagerDisguise", "&7Disguise: &3{disguise}"),
    BLOCK_MANAGER_ENABLED("BlockManagerEnabled", "&7Status: &3Enabled"),
    BLOCK_MANAGER_DISABLED("BlockManagerDisabled", "&7Status: &3Disabled"),
    BLOCK_MANAGER_MANAGE("BlockManagerManage", "&bClick to manage this block"),
    BLOCK_MANAGER_ENABLE("BlockManagerEnable", "&bClick to enable this block"),
    BLOCK_MANAGER_DISABLE("BlockManagerDisable", "&bClick to disable this block"),
    BLOCK_MANAGER_RENAME("BlockManagerRename", "&bClick to rename this block");

    private final String path;
    private final String defaultMessage;

    PluginMessages(final String path, final String defaultMessage) {
        this.path = path;
        this.defaultMessage = defaultMessage;
    }

    PluginMessages(final String path, final String[] defaultMessage) {
        this.path = path;
        this.defaultMessage = String.join("\n", defaultMessage);
    }

    /**
     * Get the file component
     *
     * @return the component
     */
    @Override
    public FileComponent getComponent() {
        return FileComponent.PLUGIN;
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
