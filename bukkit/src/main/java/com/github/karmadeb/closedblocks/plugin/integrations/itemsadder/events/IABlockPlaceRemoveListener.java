package com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events;

import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.ItemsAdderIntegration;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.event.PlaceRemoveListener;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper.BlockBreakEventWrapper;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper.BlockPlaceEventWrapper;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class IABlockPlaceRemoveListener implements Listener {

    private final ItemsAdderIntegration integration;

    public IABlockPlaceRemoveListener(final ItemsAdderIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(CustomBlockPlaceEvent e) {
        BlockPlaceEventWrapper wrapper = new BlockPlaceEventWrapper(integration.getPlugin(), e, e.getPlayer(),
                e.getBlock(), e.getPlacedAgainst());
        PlaceRemoveListener.handlePlacement(wrapper);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(CustomBlockBreakEvent e) {
        BlockBreakEventWrapper wrapper = new BlockBreakEventWrapper(integration.getPlugin(), e, e.getPlayer(),
                e.getBlock());
        if (PlaceRemoveListener.handleDestroy(wrapper, integration.getPlugin().getBukkitIntegration().createElevatorItem()))
            return;

        Block block = e.getBlock();

        e.setCancelled(true);
        CustomBlock.byAlreadyPlaced(block).playBreakEffect();
        CustomBlock.byAlreadyPlaced(block).remove();
    }
}
