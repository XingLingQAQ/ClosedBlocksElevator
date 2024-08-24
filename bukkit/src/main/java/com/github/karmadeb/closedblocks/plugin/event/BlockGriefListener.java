package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.file.configuration.elevator.ElevatorConfig;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BlockGriefListener implements Listener {

    private final ClosedBlocksPlugin plugin;

    public BlockGriefListener(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        if (ElevatorConfig.ALLOW_EXPLODE.get()) return;
        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        Block explodeCause = e.getBlock();
        if (ClosedAPI.getInstance().getBlockStorage().isClosedBlock(explodeCause) && !ElevatorConfig.ALLOW_EXPLODE.get()) {
            e.setCancelled(true);
            return;
        }

        if (ElevatorConfig.ALLOW_EXPLODE.get()) return;
        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        handleBlockBurn(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        handleBlockBurn(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrief(EntityChangeBlockEvent e) {
        if (ElevatorConfig.ALLOW_GRIEF.get()) return;
        Block block = e.getBlock();
        if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (ElevatorConfig.ALLOW_GRIEF.get()) return;
        for (Block block : e.getBlocks()) {
            if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
                e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (ElevatorConfig.ALLOW_GRIEF.get()) return;
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

    private static void handleBlockBurn(Cancellable e) {
        if (ElevatorConfig.ALLOW_BURN.get()) return;
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

    private void handleExplosion(final List<Block> blockList) {
        List<Block> toRemove = new ArrayList<>();
        for (Block block : blockList) {
            if (ClosedAPI.getInstance().getBlockStorage().isClosedBlock(block)) {
                ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
                if (cb == null) continue;

                if (ElevatorConfig.ALLOW_EXPLODE.get()) {
                    block.getDrops().clear();

                    ItemStack drop = plugin.getBukkitIntegration().createElevatorItem();
                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                } else {
                    toRemove.add(block);
                }
            }
        }

        blockList.removeAll(toRemove);
    }
}
