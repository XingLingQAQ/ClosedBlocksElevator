package com.github.karmadeb.closedblocks.plugin.util.inventory;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.file.messages.PluginMessages;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.functional.helper.Colorize;
import com.github.karmadeb.functional.inventory.helper.page.InventoryPaginated;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Optional;

class InventoryManagerHandler implements Listener {

    private final ClosedBlocksPlugin plugin;
    private final ClosedBlockManager manager;
    private boolean isRenaming = false;

    InventoryManagerHandler(final ClosedBlocksPlugin plugin, final ClosedBlockManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (isRenaming)
            return;

        HumanEntity entity = e.getPlayer();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        if (!player.equals(this.manager.getPlayer()))
            return;

        Inventory closed = e.getInventory();
        if (this.manager.isNotInventory(closed)) return;

        this.manager.finish();

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            int pageIndex = this.manager.getPage().getPage();
            InventoryPaginated paginated = this.manager.getPage().getPaginated();
            paginated.open(player, pageIndex - 1);
        }, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemClick(InventoryClickEvent e) {
        HumanEntity entity = e.getWhoClicked();
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        if (!player.equals(this.manager.getPlayer()))
            return;

        Inventory clicked = e.getInventory();
        if (this.manager.isNotInventory(clicked)) return;
        e.setCancelled(true);

        int slot = e.getSlot();
        switch (slot) {
            case 10: //Enable block
                this.manager.getBlock().getSettings().setEnabled(true);
                player.closeInventory();
                break;
            case 13: //Disable block
                this.manager.getBlock().getSettings().setEnabled(false);
                player.closeInventory();
                break;
            case 16: //Rename block
                isRenaming = true;
                player.setMetadata("cb_renaming", new FixedMetadataValue(this.plugin, true));
                player.closeInventory();
                PluginMessages.RENAME_TYPE_CHAT.send(player);
                break;
            case 31: //Cancel
                player.closeInventory();
                break;
            default:
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!isRenaming) return;

        Player player = e.getPlayer();
        if (player.equals(this.manager.getPlayer()))
            e.setCancelled(true);

        e.getRecipients()
                .removeIf((p) -> player.hasMetadata("cb_renaming"));

        String message = e.getMessage();
        if (message.equalsIgnoreCase(".cancel")) {
            isRenaming = false;
            player.removeMetadata("cb_renaming", this.plugin);
            Bukkit.getScheduler().runTask(this.plugin, () -> this.manager.open(player));

            return;
        }

        ClosedBlock block = this.manager.getBlock();
        if (block instanceof Elevator) {
            String newName = message.trim().replace("\\u00a7", "§");
            if (ChatColor.stripColor(Colorize.colorize(newName)).isEmpty()) {
                PluginMessages.RENAME_FAILED
                        .send(player,
                                MessageParameter.type(block),
                                MessageParameter.name(
                                newName.replace("&", "\\&")
                                        .replace("§", "\\§")
                        ));
                return;
            }

            Optional<Elevator> match = ClosedAPI.getInstance().getBlockStorage().getAllBlocks(BlockType.ELEVATOR)
                    .stream().filter((b) -> ChatColor.stripColor(
                            Colorize.colorize(b.getSettings().getName())
                    ).equalsIgnoreCase(
                            ChatColor.stripColor(Colorize.colorize(newName))
                    )).findAny();
            if (match.isPresent() && !match.get().equals(block)) {
                PluginMessages.RENAME_ALREADY.send(player,
                        MessageParameter.type(block),
                        MessageParameter.name(newName));
                return;
            }

            block.getSettings().setName(newName);

            isRenaming = false;
            this.manager.finish();

            player.removeMetadata("cb_renaming", this.plugin);

            int pageIndex = this.manager.getPage().getPage();
            InventoryPaginated paginated = this.manager.getPage().getPaginated();
            Bukkit.getScheduler().runTask(this.plugin, () -> paginated.open(player, pageIndex - 1));
        }
    }
}
