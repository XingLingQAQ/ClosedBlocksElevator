package com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events;

import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.BukkitIntegration;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.event.PlaceRemoveListener;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper.BlockBreakEventWrapper;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper.BlockPlaceEventWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BBlockPlaceRemoveListener implements Listener {

    private final BukkitIntegration integration;

    public BBlockPlaceRemoveListener(final BukkitIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        BlockPlaceEventWrapper wrapper = new BlockPlaceEventWrapper(integration.getPlugin(), e, e.getPlayer(),
                e.getBlock(), e.getBlockAgainst());
        PlaceRemoveListener.handlePlacement(wrapper);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockRemove(BlockBreakEvent e) {
        BlockBreakEventWrapper wrapper = new BlockBreakEventWrapper(integration.getPlugin(), e, e.getPlayer(), e.getBlock());
        if (PlaceRemoveListener.handleDestroy(wrapper))
            return;

        e.setCancelled(false);
        e.setDropItems(false);
        e.setExpToDrop(0);
    }
}
