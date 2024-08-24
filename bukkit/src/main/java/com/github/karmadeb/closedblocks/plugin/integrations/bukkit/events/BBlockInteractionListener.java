package com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
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

public class BBlockInteractionListener implements Listener {

    private final BukkitIntegration integration;

    public BBlockInteractionListener(final BukkitIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking() || !e.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        ItemStack used = e.getItem();
        if (used != null && !used.getType().equals(Material.AIR)) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        if (!ClosedAPI.getInstance().getBlockStorage().isClosedBlock(clicked)) return;

        ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(clicked).orElse(null);
        if (cb == null) return;

        OfflinePlayer owner = cb.getOwner();
        if (!owner.getUniqueId().equals(player.getUniqueId())) {
            e.setCancelled(true);
            return;
        }

        IntegrationUtils.removeElevator(integration.getPlugin(), player, cb, integration.createElevatorItem());
        clicked.setType(Material.AIR);
        integration.getPlugin().getParticleAPI().playDisguiseEffect(clicked.getWorld(), clicked.getLocation().clone().add(0.5, 0.95, 0.5));

        e.setCancelled(true);
    }
}
