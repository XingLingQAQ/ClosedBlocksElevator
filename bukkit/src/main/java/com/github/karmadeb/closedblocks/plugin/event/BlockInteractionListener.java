package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.BukkitIntegration;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.IntegrationUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BlockInteractionListener implements Listener {

    private final BukkitIntegration integration;

    public BlockInteractionListener(final BukkitIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking() || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        ItemStack used = e.getItem();
        if (used != null && !used.getType().equals(Material.AIR)) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        if (!ClosedAPI.getInstance().getBlockStorage().isClosedBlock(clicked)) return;

        ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(clicked).orElse(null);
        if (cb == null) return;

        e.setCancelled(true);
        OfflinePlayer owner = cb.getOwner();
        if (!owner.getUniqueId().equals(player.getUniqueId()))
            return;

        IntegrationUtils.removeClosedBlock(integration.getPlugin(), player, cb, ClosedAPI.createItem(cb.getType()));
        if (!(cb instanceof Mine))
            clicked.setType(Material.AIR);

        integration.getPlugin().getParticleAPI().playDisguiseEffect(clicked.getWorld(), clicked.getLocation().clone().add(0.5, 0.95, 0.5));
    }
}
