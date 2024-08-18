package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.lang.reflect.Field;

public class BlockGriefListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockIgniteEvent e) {
        Block block = null;

        try {
            Field field = e.getClass().getDeclaredField("block");
            block = (Block) field.get(e);
        } catch (Throwable ex) {
            try {
                Field field = e.getClass().getField("ignitingBlock");
                block = (Block) field.get(e);
            } catch (Throwable ignored) {}
        }

        if (block == null) return;
        if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrief(EntityChangeBlockEvent e) {
        Block block = e.getBlock();
        if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
                e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onPistonRetract(BlockPistonRetractEvent e) {
        try {
            for (Block block : e.getBlocks()) {
                if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
                    e.setCancelled(true);
            }
        } catch (Throwable ex) {
            Location retract = e.getRetractLocation();
            Block block = retract.getBlock();

            if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
                e.setCancelled(true);
        }
    }
}
