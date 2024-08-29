package com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events;

import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPlacedEvent;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.IntegrationUtils;
import com.github.karmadeb.closedblocks.plugin.util.MaterialUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class BClosedBlockPlacedListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClosedBlockPlaced(final ClosedBlockPlacedEvent e) {
        if (e.isHandled()) //Handled by another integration
            return;

        Block block = e.getBlock();
        ClosedBlock placedBlock = e.getPlacedBlock();

        if (placedBlock instanceof Mine)
            return;

        BlockSettings settings = placedBlock.getSettings();

        String disguise = settings.getDisguise();
        Material matchMaterial;
        if (placedBlock instanceof Elevator) {
            matchMaterial = MaterialUtils.getMatchingMaterial(disguise, IntegrationUtils::isIllegalElevatorType, () -> Material.QUARTZ_BLOCK);
        } else {
            return;
        }

        assert matchMaterial != null;

        block.setType(matchMaterial);
        e.handle();
    }
}
