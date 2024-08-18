package com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events;

import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPlacedEvent;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.BukkitIntegration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BClosedBlockPlacedListener implements Listener {

    private final BukkitIntegration integration;

    public BClosedBlockPlacedListener(final BukkitIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClosedBlockPlaced(final ClosedBlockPlacedEvent e) {
        if (e.isHandled()) //Handled by another integration
            return;

        Block block = e.getBlock();
        ClosedBlock placedBlock = e.getPlacedBlock();
        BlockSettings settings = placedBlock.getSettings();

        String disguise = settings.getDisguise();
        Material matchMaterial = integration.getMatchingMaterial(disguise, integration::isValidMaterial, () -> Material.QUARTZ_BLOCK);
        assert matchMaterial != null;

        block.setType(matchMaterial);
        e.handle();
    }
}
