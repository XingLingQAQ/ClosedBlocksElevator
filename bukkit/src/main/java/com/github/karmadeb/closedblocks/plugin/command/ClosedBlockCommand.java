package com.github.karmadeb.closedblocks.plugin.command;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.file.messages.PluginMessages;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.api.item.ItemType;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.util.inventory.ClosedBlockManager;
import com.github.karmadeb.functional.helper.Colorize;
import com.github.karmadeb.functional.inventory.helper.PagedInventory;
import com.github.karmadeb.functional.inventory.helper.functional.Action;
import com.github.karmadeb.functional.inventory.helper.page.InventoryPaginated;
import com.github.karmadeb.functional.inventory.helper.page.type.PageAction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ClosedBlockCommand implements CommandExecutor, TabCompleter {

    private static final String RELOAD_PERMISSION = "closedblocks.reload";
    private static final String GIVE_ELEVATOR_PERMISSION = "closedblocks.give.elevator";
    private static final String GIVE_MINE_PERMISSION = "closedblocks.give.mine";
    private static final String GIVE_DIFFUSER_PERMISSION = "closedblocks.give.diffuser";

    private static final String[] ACTIONS = new String[]{"help", "give", "reload", "manage"};
    private static final String[] HELP_TYPES = new String[]{"give", "reload", "manage"};
    private static final String[] GIVE_TYPES = new String[]{"elevator", "mine", "diffuser"};

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

    private void handleActionManageOnly(final @NotNull CommandSender sender, final String name) {
        if (!(sender instanceof Player)) {
            PluginMessages.PLAYER_REQUIRED.send(sender);
            return;
        }

        Player player = (Player) sender;
        List<ClosedBlock> playerOwnedBlocks = ClosedAPI.getInstance().getBlockStorage().getAllBlocks(player);
        if (name != null)
            playerOwnedBlocks = playerOwnedBlocks.stream()
                    .filter((block) -> ChatColor.stripColor(color(block.getSettings().getName())).toLowerCase()
                            .startsWith(ChatColor.stripColor(color(name)).toLowerCase())).collect(Collectors.toList());

        InventoryPaginated paginated = new InventoryPaginated(plugin);
        paginated.allowClose();

        int pages = playerOwnedBlocks.size() / 45;
        int extra = playerOwnedBlocks.size() % 45;
        if (extra > 0)
            pages += 1;

        if (pages == 0)
            pages = 1;

        int vItem = 0;

        ItemStack elevatorItem = ClosedAPI.createItem(BlockType.ELEVATOR);
        ItemStack mineItem = ClosedAPI.createItem(BlockType.MINE);
        assert elevatorItem != null;
        assert mineItem != null;

        for (int i = 0; i < pages; i++) {
            PagedInventory<InventoryPaginated> page = paginated.addPage();

            for (int j = 0; j <= 45; j++) {
                if (vItem >= playerOwnedBlocks.size())
                    break;

                ClosedBlock block = playerOwnedBlocks.get(vItem++);
                ItemStack item;
                if (block instanceof Elevator) {
                    item = elevatorItem.clone();
                } else {
                    item = mineItem.clone();
                }

                mapBlockInfoToItem(block, item);

                page.setItem(j, item).onClick(createClickAction(player, page,
                        block, j, item));
            }
        }

        paginated.open(player);
    }

    private Action<PagedInventory<InventoryPaginated>> createClickAction(final Player player, final PagedInventory<InventoryPaginated> page,
                                                                         final ClosedBlock block, final int itemSlot, final ItemStack item) {
        return PageAction.create(plugin)
                .runNow(() -> {
                    ClosedBlockManager manager = new ClosedBlockManager(this.plugin, player, page,
                            block, () -> {
                        if (block.getSaveData().saveBlockData()) {
                            mapBlockInfoToItem(block, item);
                            page.setItem(itemSlot, item).onClick(
                                    createClickAction(player, page, block, itemSlot, item)
                            );
                        }
                    });
                    manager.open(player);
                });
    }

    private void mapBlockInfoToItem(final ClosedBlock block, final ItemStack item) {
        BlockSettings settings = block.getSettings();

        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        String name = String.format("&7Block at&3 %d&7,&3 %d&7,&3 %d",
                    block.getX(), block.getY(), block.getZ());

        meta.setDisplayName(color(name));

        List<String> lore = new ArrayList<>();
        lore.add(color("&0&m--------------"));
        addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_NAME.parse(
                MessageParameter.name(settings.getName().isEmpty() ?
                        name : settings.getName())
        ));
        addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_TYPE.parse(
                MessageParameter.type(block)
        ));
        addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_WORLD.parse(
                MessageParameter.world(block.getWorld())
        ));
        addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_COORDS.parse(
                MessageParameter.x(block.getX()),
                MessageParameter.y(block.getY()),
                MessageParameter.z(block.getZ())
        ));
        addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_DISGUISE.parse(
                MessageParameter.disguise(settings.getDisguiseName())
        ));

        boolean addStatus = true;
        if (block instanceof Mine) {
            Mine mine = (Mine) block;
            addStatus = mapMineInfoToItem(lore, mine);
        }

        if (addStatus) {
            if (settings.isEnabled()) {
                addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_ENABLED.parse());
            } else {
                addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_DISABLED.parse());
            }
        }

        lore.add(color("&0&m--------------"));
        lore.add(PluginMessages.BLOCK_MANAGER_MANAGE.parse());
        meta.setLore(lore);

        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
    }

    private boolean mapMineInfoToItem(List<String> lore, Mine mine) {
        addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_POWER.parse(
                MessageParameter.power(mine)
        ));
        if (mine.causesFire()) {
            addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_INCENDIARY.parse());
        } else {
            addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_NOT_INCENDIARY.parse());
        }

        if (mine.isDefused()) {
            addIfNotEmpty(lore, PluginMessages.BLOCK_MANAGER_DEFUSED.parse());
            return false;
        }

        return true;
    }

    private String color(final String string) {
        return Colorize.colorize(string);
    }

    private void addIfNotEmpty(final List<String> list, final String content) {
        if (content.trim().isEmpty())
            return;

        list.add(color(content));
    }

    private void sendHelp(final CommandSender sender, final String label) {
        PluginMessages.HELP_TITLE.send(sender);
        PluginMessages.HELP_INLINE_GIVE.send(sender,
                MessageParameter.label(label));
        PluginMessages.HELP_INLINE_RELOAD.send(sender,
                MessageParameter.label(label));
        PluginMessages.HELP_INLINE_MANAGE.send(sender,
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
                case "manage":
                    PluginMessages.HELP_TITLE.send(sender);
                    PluginMessages.HELP_MANAGE.send(sender);
                    break;
                default:
                    sendHelp(sender, label);
            }
        } else if (action.equalsIgnoreCase("give")) {
            PluginMessages.INCOMPLETE_ACTION.send(sender, MessageParameter.label(label),
                    MessageParameter.action(action));
        } else if (action.equalsIgnoreCase("manage")) {
            handleActionManageOnly(sender, actionParam);
        }
    }

    private void handleActionOnly(@NotNull CommandSender sender, @NotNull String label, String action) {
        if (action.equalsIgnoreCase("help")) {
            sendHelp(sender, label);
            return;
        } else if (action.equalsIgnoreCase("reload")) {
            reloadPlugin(sender);
            return;
        } else if (action.equalsIgnoreCase("manage")) {
            handleActionManageOnly(sender, null);
            return;
        }

        PluginMessages.INCOMPLETE_ACTION.send(sender, MessageParameter.label(label),
                MessageParameter.action(action));
    }

    private void handleActionGiveOnly(final CommandSender sender, final String blockType, final String target, final int amount) {
        Player player;
        ItemStack stack;

        BlockType<?> bt = null;
        ItemType it = null;

        switch (blockType.toLowerCase()) {
            case "elevator":
                if (!sender.hasPermission(GIVE_ELEVATOR_PERMISSION)) {
                    PluginMessages.PERMISSION.send(sender,
                            MessageParameter.permission(GIVE_ELEVATOR_PERMISSION));
                    return;
                }

                bt = BlockType.ELEVATOR;
                player = Bukkit.getPlayer(target);
                stack = ClosedAPI.createItem(BlockType.ELEVATOR);
                break;
            case "mine":
                if (!sender.hasPermission(GIVE_MINE_PERMISSION)) {
                    PluginMessages.PERMISSION.send(sender,
                            MessageParameter.permission(GIVE_MINE_PERMISSION));
                    return;
                }

                bt = BlockType.MINE;
                player = Bukkit.getPlayer(target);
                stack = ClosedAPI.createItem(BlockType.MINE);
                break;
            case "diffuser":
                if (!sender.hasPermission(GIVE_DIFFUSER_PERMISSION)) {
                    PluginMessages.PERMISSION.send(sender,
                            MessageParameter.permission(GIVE_DIFFUSER_PERMISSION));
                    return;
                }

                it = ItemType.DIFFUSER;
                player = Bukkit.getPlayer(target);
                stack = ClosedAPI.createItem(ItemType.DIFFUSER);
                break;
            default:
                PluginMessages.GIVE_UNKNOWN_BLOCK.send(sender,
                        MessageParameter.player(target),
                        MessageParameter.type(blockType));
                return;
        }

        if (player == null || !player.isOnline()) {
            PluginMessages.GIVE_PLAYER_OFFLINE.send(sender,
                    MessageParameter.player(target));
            return;
        }

        int available = getAvailableSlots(player, stack);
        if (available >= amount) {
            if (bt != null) {
                plugin.getDiscordSRVIntegration().grantBlockEmbed(sender, player, bt, amount);
            } else {
                plugin.getDiscordSRVIntegration().grantItemEmbed(sender, player, it, amount);
            }

            giveBlocks(sender, blockType, amount, stack, player);
        } else {
            PluginMessages.GIVE_INVENTORY_FULL.send(sender,
                    MessageParameter.player(player),
                    MessageParameter.slots(available));
        }
    }

    private static void giveBlocks(CommandSender sender, String blockType, int amount, ItemStack stack, Player player) {
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
        ClosedAPI.getInstance().getRecipeManager().reloadRecipes();

        PluginMessages.RELOAD_SUCCESS.send(sender);
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
