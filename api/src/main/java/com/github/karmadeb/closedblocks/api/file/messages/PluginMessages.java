package com.github.karmadeb.closedblocks.api.file.messages;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;

/**
 * Represents a plugin message
 */
public enum PluginMessages implements MessageDeclaration {
    PERMISSION("Permission", "&dYou don't have the permission &7{permission}"),
    INVALID_ACTION("InvalidAction", "&dInvalid action. Valid actions are&7: {actions}"),
    INCOMPLETE_ACTION("IncompleteAction", "&dInvalid or incomplete action. Run &8\"&7{label} help {action}&8\"&d for help"),
    HELP_TITLE("HelpTitle", "&8&m------&3 ClosedBlocks &8&m------"),
    HELP_INLINE_GIVE("HelpInlineGive", "&8/&7{label} give &8- &fGives a closed block to a player"),
    HELP_INLINE_RELOAD("HelpInlineReload", "&8/&7{label} reload &8- &fReloads the plugin files and recipes"),
    HELP_GIVE("HelpGive", new String[]{
            "&7Gives the&d [player]&7 a closed block",
            "&dParameters:",
            "  &8-&7 <&eblock type&7> &8- &fThe closed block type",
            "  &8-&7 <&eplayer&7>     &8- &fThe player to give the elevator",
            "  &8-&7 [&eamount&7]     &8- &fThe amount of blocks to give",
            "&dPermissions:",
            "  &8- &7closedblocks.give",
            "  &8- &7closedblocks.give.&3<&dblock type&3>",
    }),
    HELP_RELOAD("HelpReload", new String[]{
            "&7Reloads the plugin files and recipes",
            "&dPermissions:",
            "  &8- &7closedblocks.reload"
    }),
    RELOAD_SUCCESS("ReloadSuccess", "&3Successfully reloaded ClosedBlocks files and recipes"),
    RELOAD_FAILED("ReloadFailed", "&dFailed to reload ClosedBlocks files and recipes"),
    GIVE_UNKNOWN_BLOCK("GiveUnknownBlock", "&dFailed to give closed block to&7 {player}&d. Unknown type:&7 {type}"),
    GIVE_PLAYER_OFFLINE("GivePlayerOffline", "&dFailed to give closed block to&7 {player}&d. Player is offline"),
    GIVE_INVALID_AMOUNT("GiveInvalidAmount", "&dFailed to give closed block to&7 {player}&d. Specified amount is invalid"),
    GIVE_INVENTORY_FULL("GiveInventoryFull", "&dFailed to give closed block to&7 {player}&d. Target inventory only has &7{slots}&d available slots"),
    GIVE_SUCCESS("GiveSuccess", "&3Successfully give&7 {amount}&3 blocks of &7{type}&3 to&7 {player}"),
    GIVE_RECEIVED("GiveReceived", "&3You've received &7{amount}&3 blocks of &7{type}&3 from&7 {player}");

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
