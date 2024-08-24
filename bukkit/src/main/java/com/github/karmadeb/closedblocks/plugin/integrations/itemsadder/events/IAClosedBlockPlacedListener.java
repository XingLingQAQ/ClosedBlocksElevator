package com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.events;

import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPlacedEvent;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class IAClosedBlockPlacedListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClosedBlockPlaced(final ClosedBlockPlacedEvent e) {
        if (e.isHandled()) //Handled by another integration
            return;

        Block block = e.getBlock();
        ClosedBlock placedBlock = e.getPlacedBlock();
        BlockSettings settings = placedBlock.getSettings();

        String disguise = settings.getDisguise();
        if (CustomBlock.isInRegistry(disguise)) {
            CustomBlock custom = CustomBlock.getInstance(disguise);
            custom.place(block.getLocation());

            e.handle();
        }
    }
}
