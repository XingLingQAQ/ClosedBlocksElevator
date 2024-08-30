package com.github.karmadeb.closedblocks.plugin.util.inventory;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.file.messages.PluginMessages;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.functional.helper.Colorize;
import com.github.karmadeb.functional.inventory.helper.PagedInventory;
import com.github.karmadeb.functional.inventory.helper.functional.PageItemMeta;
import com.github.karmadeb.functional.inventory.helper.page.InventoryPaginated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClosedBlockManager {

    private final Inventory inventory;
    private final Player player;
    private final PagedInventory<InventoryPaginated> page;
    private final PageItemMeta meta;
    private final ClosedBlock block;
    private final InventoryManagerHandler handler;
    private final Runnable onFinish;

    public ClosedBlockManager(final ClosedBlocksPlugin plugin, final Player player,
                              final PagedInventory<InventoryPaginated> page, final PageItemMeta meta,
                              final ClosedBlock block, final Runnable onFinish) {
        BlockSettings settings = block.getSettings();
        this.inventory = Bukkit.createInventory(null, 36, Colorize.colorize(
                settings.getName().isEmpty() ?
                        String.format("&7Block at&3 %d&7,&3 %d&7,&3 %d",
                                block.getX(), block.getY(), block.getZ()) : settings.getName()));
        this.mapItems();

        this.player = player;
        this.page = page;
        this.meta = meta;
        this.block = block;
        this.handler = new InventoryManagerHandler(plugin, this);
        this.onFinish = onFinish;

        Bukkit.getServer().getPluginManager().registerEvents(this.handler, plugin);
    }

    private void mapItems() {
        ItemStack enable = new ItemStack(Material.GREEN_CONCRETE);
        ItemStack disable = new ItemStack(Material.RED_CONCRETE);
        ItemStack rename = new ItemStack(Material.WRITABLE_BOOK);
        ItemStack cancel = new ItemStack(Material.BARRIER);

        apply(enable, PluginMessages.BLOCK_MANAGER_ENABLE);
        apply(disable, PluginMessages.BLOCK_MANAGER_DISABLE);
        apply(rename, PluginMessages.BLOCK_MANAGER_RENAME);
        apply(cancel, PluginMessages.BLOCK_MANAGER_CANCEL);

        this.inventory.setItem(10, enable);
        this.inventory.setItem(13, disable);
        this.inventory.setItem(16, rename);
        this.inventory.setItem(31, cancel);
    }

    private void apply(final ItemStack item, final PluginMessages message) {
        ItemMeta meta = item.getItemMeta();
        assert meta != null;

        meta.setDisplayName(Colorize.colorize( message.parse()));
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
    }

    public PagedInventory<InventoryPaginated> getPage() {
        return this.page;
    }

    public PageItemMeta getMeta() {
        return this.meta;
    }

    public Player getPlayer() {
        return this.player;
    }

    public ClosedBlock getBlock() {
        return this.block;
    }

    public boolean isNotInventory(final Inventory inventory) {
        return !this.inventory.equals(inventory);
    }

    public void open(final Player player) {
        player.openInventory(this.inventory);
    }

    public void finish() {
        HandlerList.unregisterAll(this.handler);
        onFinish.run();
    }
}
