package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.file.configuration.elevator.ElevatorConfig;
import com.github.karmadeb.closedblocks.api.file.configuration.mine.MineConfig;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.util.BlockUtils;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockGriefListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        Block explodeCause = e.getBlock();
        if (ClosedAPI.getInstance().getBlockStorage().isClosedBlock(explodeCause)) {
            ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(explodeCause).orElse(null);
            if (cb instanceof Elevator && !ElevatorConfig.ALLOW_EXPLODE.get()) {
                e.setCancelled(true);
                return;
            }
        }

        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        handleBlockBurn(e, e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        handleBlockBurn(e, e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onGrief(EntityChangeBlockEvent e) {
        Block block = e.getBlock();
        if (ClosedAPI.getInstance().getBlockStorage().isClosedBlock(block)) {
            ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
            if (cb == null) return;

            if (cb instanceof Elevator) {
                e.setCancelled(!ElevatorConfig.ALLOW_GRIEF.get());
            } else if (cb instanceof Mine) {
                Mine mine = (Mine) cb;
                if (MineConfig.PICKUP_EXPLODE.get() && ClosedAPI.getInstance().getBlockStorage().destroyBlock(mine))
                    BlockUtils.explodeMine(mine);

                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            handlePiston(e, block);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            handlePiston(e, block);
        }
    }

    @SuppressWarnings("t")
    private void handleBlockBurn(final Cancellable e, Block block) {
        if (block == null) return;

        for (BlockFace face : BlockFace.values()) {
            Block target = block.getRelative(face);

            if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(target)) {
                ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(target).orElse(null);
                if (cb == null) return;

                if (cb instanceof Elevator) {
                    e.setCancelled(!ElevatorConfig.ALLOW_BURN.get());
                    return;
                } else if (cb instanceof Mine) {
                    Mine mine = (Mine) cb;

                    if (MineConfig.IGNITE_EXPLODE.get() && ClosedAPI.getInstance().getBlockStorage().destroyBlock(mine))
                        BlockUtils.explodeMine(mine);
                }
            }
        }
    }

    @SuppressWarnings("t")
    private void handleExplosion(final List<Block> blockList) {
        List<Block> toRemove = new ArrayList<>();
        for (Block block : blockList) {
            if (ClosedAPI.getInstance().getBlockStorage().isClosedBlock(block)) {
                ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
                if (cb == null) continue;

                if (cb instanceof Elevator) {
                    handleElevatorExplosion(block, toRemove);
                } else if (cb instanceof Mine) {
                    Mine mine = (Mine) cb;

                    if (MineConfig.CHAIN_EXPLOSION.get() && ClosedAPI.getInstance().getBlockStorage().destroyBlock(mine)) {
                        BlockUtils.explodeMine(mine);
                    }
                }
            }
        }

        blockList.removeAll(toRemove);
    }

    private void handlePiston(final Cancellable e, final Block block) {
        if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block)) {
            ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
            if (cb == null) return;

            if (cb instanceof Elevator) {
                if (ElevatorConfig.ALLOW_GRIEF.get())
                    e.setCancelled(true);
            } else if (cb instanceof Mine) {
                Mine mine = (Mine) cb;
                if (MineConfig.PICKUP_EXPLODE.get() && ClosedAPI.getInstance().getBlockStorage().destroyBlock(mine))
                    BlockUtils.explodeMine(mine);
            }
        }
    }

    private void handleElevatorExplosion(final Block block, final List<Block> toRemove) {
        if (ElevatorConfig.ALLOW_EXPLODE.get()) {
            block.getDrops().clear();
            block.getDrops().add(ClosedAPI.createItem(BlockType.ELEVATOR));
        } else {
            toRemove.add(block);
        }
    }
}
