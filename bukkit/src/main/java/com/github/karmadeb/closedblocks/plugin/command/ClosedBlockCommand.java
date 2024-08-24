package com.github.karmadeb.closedblocks.plugin.command;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.file.messages.PluginMessages;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class ClosedBlockCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "closedblocks.reload";
    private static final String GIVE_ELEVATOR_PERMISSION = "closedblocks.give.elevator";

    private static final String[] ACTIONS = new String[]{"help", "give", "reload"};
    private static final String[] HELP_TYPES = new String[]{"give", "reload"};
    private static final String[] GIVE_TYPES = new String[]{"elevator"};

    private final ClosedBlocksPlugin plugin;

    public ClosedBlockCommand(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (args.length == 0) {
            PluginMessages.INVALID_ACTION.send(sender, MessageParameter.actions(ACTIONS));
            return false;
        }

        String action = args[0];
        switch (args.length) {
            case 1:
                handleActionOnly(sender, label, action);
                break;
            case 2:
                handleActionParamOnly(sender, label, args, action);
                break;
            case 3:
            case 4:
                String blockType = args[1];
                String targetPlayer = args[2];
                String rawAmount = "1";
                if (args.length == 4) {
                    rawAmount = args[3];
                }

                try {
                    int amount = Integer.parseInt(rawAmount);
                    if (amount <= 0) {
                        PluginMessages.GIVE_INVALID_AMOUNT.send(sender, MessageParameter
                                .player(targetPlayer));
                        return false;
                    }

                    handleActionGiveOnly(sender, blockType, targetPlayer, amount);
                } catch (NumberFormatException ex) {
                    PluginMessages.GIVE_INVALID_AMOUNT.send(sender, MessageParameter
                            .player(targetPlayer));
                }

                break;
            default:
                sendHelp(sender, label);
                break;
        }

        return false;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public @Nullable List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, final @NotNull String[] args) {
        if (args.length == 0)
            return Arrays.asList(ACTIONS);

        String param = args[0].toLowerCase();
        if (args.length == 1) {
            return Arrays.stream(ACTIONS)
                    .filter((act) -> act.startsWith(param))
                    .collect(Collectors.toList());
        }

        String subParam = args[1].toLowerCase();
        if (args.length == 2) {
            switch (param) {
                case "help":
                    return Arrays.stream(HELP_TYPES)
                            .filter((act) -> act.startsWith(subParam))
                            .collect(Collectors.toList());
                case "give":
                    return Arrays.stream(GIVE_TYPES)
                            .filter((act) -> act.startsWith(subParam))
                            .collect(Collectors.toList());
                default:
                    return Collections.emptyList();
            }
        }

        if (param.equalsIgnoreCase("give") && args.length == 3) {
            String target = args[2].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter((name) -> name.toLowerCase().startsWith(target))
                    .collect(Collectors.toList());
        }

        if (param.equalsIgnoreCase("give") && args.length == 4) {
            String quantity = args[3].toLowerCase();
            if (quantity.trim().isEmpty())
                return Collections.singletonList("[amount]");
        }

        return Collections.emptyList();
    }

    private void sendHelp(final CommandSender sender, final String label) {
        PluginMessages.HELP_TITLE.send(sender);
        PluginMessages.HELP_INLINE_GIVE.send(sender,
                MessageParameter.label(label));
        PluginMessages.HELP_INLINE_RELOAD.send(sender,
                MessageParameter.label(label));
    }

    private void handleActionParamOnly(@NotNull CommandSender sender, @NotNull String label, @NotNull String @NotNull [] args, String action) {
        String actionParam = args[1];
        if (action.equalsIgnoreCase("help")) {
            switch (actionParam.toLowerCase()) {
                case "give":
                    PluginMessages.HELP_TITLE.send(sender);
                    PluginMessages.HELP_GIVE.send(sender);
                    break;
                case "reload":
                    PluginMessages.HELP_TITLE.send(sender);
                    PluginMessages.HELP_RELOAD.send(sender);
                    break;
                default:
                    sendHelp(sender, label);
            }
        }
    }

    private void handleActionOnly(@NotNull CommandSender sender, @NotNull String label, String action) {
        if (action.equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return;
        } else if (action.equalsIgnoreCase("reload")) {
            reloadPlugin(sender);
            return;
        }

        PluginMessages.INCOMPLETE_ACTION.send(sender, MessageParameter.label(label),
                MessageParameter.action(action));
    }

    private void handleActionGiveOnly(final CommandSender sender, final String blockType, final String target, final int amount) {
        switch (blockType.toLowerCase()) {
            case "elevator":
                if (!sender.hasPermission(GIVE_ELEVATOR_PERMISSION)) {
                    PluginMessages.PERMISSION.send(sender,
                            MessageParameter.permission(GIVE_ELEVATOR_PERMISSION));
                    return;
                }

                Player player = Bukkit.getPlayer(target);
                ItemStack stack = plugin.getBukkitIntegration().createElevatorItem();
                if (player == null || !player.isOnline()) {
                    PluginMessages.GIVE_PLAYER_OFFLINE.send(sender,
                            MessageParameter.player(target));
                    return;
                }

                int available = getAvailableSlots(player, stack);
                if (available >= amount) {
                    giveElevator(sender, blockType, amount, stack, player);
                } else {
                    PluginMessages.GIVE_INVENTORY_FULL.send(sender,
                            MessageParameter.player(player),
                            MessageParameter.slots(available));
                }
                break;
            default:
                PluginMessages.GIVE_UNKNOWN_BLOCK.send(sender,
                        MessageParameter.player(target),
                        MessageParameter.type(blockType));
                break;
        }
    }

    private static void giveElevator(CommandSender sender, String blockType, int amount, ItemStack stack, Player player) {
        int stacks = amount / 64;
        int remaining = amount % 64;

        while (stacks-- > 0) {
            ItemStack toGive = stack.clone();
            toGive.setAmount(64);

            player.getInventory().addItem(toGive);
        }

        if (remaining > 0) {
            ItemStack toGive = stack.clone();
            toGive.setAmount(remaining);

            player.getInventory().addItem(toGive);
        }

        if (!player.equals(sender)) {
            PluginMessages.GIVE_SUCCESS.send(sender,
                    MessageParameter.player(player),
                    MessageParameter.amount(amount),
                    MessageParameter.type(blockType));
        }

        PluginMessages.GIVE_RECEIVED.send(player,
                MessageParameter.amount(amount),
                MessageParameter.type(blockType),
                MessageParameter.player(sender.getName()));
    }

    private void reloadPlugin(@NotNull CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            PluginMessages.PERMISSION.send(sender,
                    MessageParameter.permission(RELOAD_PERMISSION));
            return;
        }

        ClosedAPI.getInstance().getConfig().reload();
        ClosedAPI.getInstance().getMessages().reload();
        if (plugin.getBukkitIntegration().reloadRecipes()) {
            PluginMessages.RELOAD_SUCCESS.send(
                    sender
            );
        } else {
            PluginMessages.RELOAD_FAILED.send(
                    sender
            );
        }
    }

    private int getAvailableSlots(final Player player, final ItemStack forItem) {
        PlayerInventory inventory = player.getInventory();
        int available = 0;
        for (int i = 0; i <= 35; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType().equals(Material.AIR)) {
                available += 64;
                continue;
            }

            if (item.getAmount() >= 64)
                continue;

            if (item.isSimilar(forItem))
                available += (64 - item.getAmount());
        }

        return available;
    }
}
