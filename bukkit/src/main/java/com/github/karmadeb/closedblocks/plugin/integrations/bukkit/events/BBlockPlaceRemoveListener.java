package com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events;

import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockDisguisedEvent;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPlacedEvent;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPreparePlaceEvent;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.BukkitIntegration;
import com.github.karmadeb.closedblocks.plugin.provider.block.ElevatorBlock;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BBlockPlaceRemoveListener implements Listener {

    private final BukkitIntegration integration;

    public BBlockPlaceRemoveListener(final BukkitIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (player.isSneaking()) return;

        //Legacy compatibility
        ItemStack mainHand = player.getInventory().getItem(player.getInventory().getHeldItemSlot());
        if (mainHand == null)
            return;

        boolean isClosedBlock = NBT.get(mainHand, (Function<ReadableItemNBT, Boolean>) (nbt) -> nbt.hasTag("closed_type"));
        if (!isClosedBlock) {
            Block against = e.getBlockAgainst();
            if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(against)) {
                ClosedBlock cb = ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(against).orElse(null);
                if (cb == null) return;

                handleElevatorDisguise(player, mainHand, e.getBlockPlaced(), cb);
                e.setCancelled(true);
            }

            return;
        }

        String type = NBT.get(mainHand, (Function<ReadableItemNBT, String>) (nbt) -> nbt.getString("closed_type"));
        switch (type.toLowerCase()) {
            case "elevator":
                ClosedBlockPreparePlaceEvent event = new ClosedBlockPreparePlaceEvent(player, e.getBlock());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }

                handleElevatorPlace(e);
                break;
            default:
                break;
                //TODO: Add mroe types
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockRemove(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (!ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
            return;

        ClosedBlock cb = ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
        if (cb == null) return;

        OfflinePlayer owner = cb.getOwner();
        if (!owner.getUniqueId().equals(player.getUniqueId())) return;

        if (!ClosedBlocksAPI.getInstance().getBlockStorage().destroyBlock(cb))
            e.setCancelled(true);
    }

    private void handleElevatorDisguise(final Player player, final ItemStack item, final Block placed, final ClosedBlock block) {
        OfflinePlayer owner = block.getOwner();
        if (!owner.getUniqueId().equals(player.getUniqueId()))
            return;

        Material material = item.getType();
        if (!material.isBlock())
            return;

        BlockSettings settings = block.getSettings();
        settings.setDisguise(material.name());

        World world = block.getWorld();
        Block blockAt = world.getBlockAt(block.getX(), block.getY(), block.getZ());

        ClosedBlockDisguisedEvent event = new ClosedBlockDisguisedEvent(player, blockAt, block);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        if (block.getSaveData().saveBlockData()) {
            blockAt.setType(material);
            blockAt.setBlockData(placed.getBlockData());
        }
    }

    private void handleElevatorPlace(final BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        List<ElevatorBlock> elevatorsInLine = ClosedBlocksAPI.getInstance().getBlockStorage()
                .getAllBlocks(block.getWorld(), Elevator.class).stream().filter((b) -> b.getX() == x && b.getZ() == z)
                .sorted(Comparator.comparingInt(ClosedBlock::getY))
                .map(ElevatorBlock.class::cast)
                .collect(Collectors.toList());

        ElevatorBlock previous = null;
        ElevatorBlock next = null;

        int level = 0;
        for (ElevatorBlock eb : elevatorsInLine) {
            if (eb.getY() < y) {
                previous = eb;
                level++;
            }

            if (eb.getY() > y) {
                next = eb;
                break;
            }
        }

        ElevatorBlock eb = new ElevatorBlock(player, world, x, y, z, integration.getPlugin());
        if (ClosedBlocksAPI.getInstance().getBlockStorage().placeBlock(eb)) {
            eb.setLevel(level);
            eb.setPrevious(previous);
            eb.setNext(next);

            if (previous != null)
                previous.setNext(eb);

            ElevatorBlock prv = eb;
            ElevatorBlock nxt = next;
            while (nxt != null) {
                nxt.setLevel(nxt.getLevel() + 1);
                nxt.setPrevious(prv);

                prv = nxt;
                nxt = (ElevatorBlock) nxt.getNext().orElse(null);
            }

            block.setMetadata("closed_type", new FixedMetadataValue(integration.getPlugin(), "elevator"));

            ClosedBlockPlacedEvent placedEvent = new ClosedBlockPlacedEvent(player, block, eb);
            Bukkit.getPluginManager().callEvent(placedEvent);
        } else {
            event.setCancelled(true);
        }
    }
}
