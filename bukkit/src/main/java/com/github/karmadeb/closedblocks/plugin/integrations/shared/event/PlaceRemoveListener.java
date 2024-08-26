package com.github.karmadeb.closedblocks.plugin.integrations.shared.event;

import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockDisguisedEvent;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPlacedEvent;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPreparePlaceEvent;
import com.github.karmadeb.closedblocks.api.file.configuration.elevator.ElevatorConfig;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.api.file.messages.elevator.ElevatorMessage;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.IntegrationUtils;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper.BlockBreakEventWrapper;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper.BlockPlaceEventWrapper;
import com.github.karmadeb.closedblocks.plugin.provider.block.ElevatorBlock;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SwitchStatementWithTooFewBranches")
public class PlaceRemoveListener {

    public static void handlePlacement(final BlockPlaceEventWrapper e) {
        Player player = e.getPlayer();

        //Legacy compatibility
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack mainHand = player.getInventory().getItem(slot);
        if (mainHand == null)
            return;

        boolean isClosedBlock = NBT.get(mainHand, (Function<ReadableItemNBT, Boolean>) (nbt) -> nbt.hasTag("closed_type"));
        if (!isClosedBlock) {
            if (player.isSneaking()) return;

            Block against = e.getPlacedAgainst();
            if (ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(against)) {
                ClosedBlock cb = ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(against).orElse(null);
                if (cb == null) return;

                handleElevatorDisguise(e.getPlugin(), player, mainHand, e.getPlacedAgainst(), cb);
                e.setCancelled(true);
            }

            return;
        }

        if (player.isSneaking()) return;

        String type = NBT.get(mainHand, (Function<ReadableItemNBT, String>) (nbt) -> nbt.getString("closed_type"));
        switch (type.toLowerCase()) {
            case "elevator":
                e.setCancelled(true);
                ClosedBlockPreparePlaceEvent event = new ClosedBlockPreparePlaceEvent(player, e.getBlock());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    ElevatorMessage.PLACEMENT_FAILED.send(player);
                    return;
                }

                handleElevatorPlace(e, slot, mainHand);
                break;
            default:
                break;
            //TODO: Add more types
        }
    }

    public static boolean handleDestroy(final BlockBreakEventWrapper e, final ItemStack elevatorItem) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (!ClosedBlocksAPI.getInstance().getBlockStorage().isClosedBlock(block))
            return true;

        ClosedBlock cb = ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
        if (cb == null) return true;

        e.setCancelled(true);
        OfflinePlayer owner = cb.getOwner();
        if (!ElevatorConfig.ALLOW_BREAK.get() && !owner.getUniqueId().equals(player.getUniqueId()) &&
                !player.hasPermission("closedblocks.destroy.all")) {
            ElevatorMessage.DESTROY_NOT_ALLOWED.send(player);
            return true;
        }

        removeIfCustomBlock(e.getPlugin(), block);

        IntegrationUtils.removeElevator(e.getPlugin(), player, cb, elevatorItem);
        return false;
    }

    private static void handleElevatorDisguise(final ClosedBlocksPlugin plugin, final Player player, final ItemStack item, final Block placed, final ClosedBlock block) {
        OfflinePlayer owner = block.getOwner();
        if (!owner.getUniqueId().equals(player.getUniqueId()) && !player.hasPermission("closedblocks.destroy.all")) {
            if (block.getSettings().canBeSeen(player))
                ElevatorMessage.DISGUISE_NOT_ALLOWED.send(player);

            return;
        }

        if (plugin.getItemsAdderIntegration().isSupported() &&
                handleItemsAdderDisguise(plugin, player, item, block))
            return;

        Material material = item.getType();
        if (IntegrationUtils.isIllegalType(material)) {
            ElevatorMessage.DISGUISE_FAILED_TYPE.send(player);
            return;
        }

        BlockSettings settings = block.getSettings();
        String previousDisguise = settings.getDisguise();
        if (previousDisguise.equals(material.name()))
            return;

        settings.setDisguise(material.name());

        World world = block.getWorld();
        Block blockAt = world.getBlockAt(block.getX(), block.getY(), block.getZ());

        ClosedBlockDisguisedEvent event = new ClosedBlockDisguisedEvent(player, blockAt, block);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            settings.setDisguise(previousDisguise);
            ElevatorMessage.DISGUISE_FAILED.send(player);
            return;
        }

        if (block.getSaveData().saveBlockData()) {
            ElevatorMessage.DISGUISE_SUCCESS.send(player);
            removeIfCustomBlock(plugin, blockAt);

            blockAt.setType(material);
            blockAt.setBlockData(placed.getBlockData());

            plugin.getParticleAPI()
                    .playDisguiseEffect(world, blockAt.getLocation().clone().add(0.5, 0.95, 0.5));
        } else {
            settings.setDisguise(previousDisguise);
            ElevatorMessage.DISGUISE_FAILED.send(player);
        }
    }

    private static void removeIfCustomBlock(final ClosedBlocksPlugin plugin, final Block blockAt) {
        if (plugin.getItemsAdderIntegration().isSupported()) {
            CustomBlock customPlaced = CustomBlock.byAlreadyPlaced(blockAt);
            if (customPlaced != null) customPlaced.remove(); //In cases an ItemsAdder block was disguised...
        }
    }

    private static boolean handleItemsAdderDisguise(final ClosedBlocksPlugin plugin, final Player player, final ItemStack item, final ClosedBlock block) {
        CustomBlock customBlock = CustomBlock.byItemStack(item);
        if (customBlock == null) return false;

        BlockSettings settings = block.getSettings();
        String previousDisguise = settings.getDisguise();
        if (previousDisguise.equals(customBlock.getNamespacedID()))
            return true;

        settings.setDisguise(customBlock.getNamespacedID());

        World world = block.getWorld();
        Block blockAt = world.getBlockAt(block.getX(), block.getY(), block.getZ());

        ClosedBlockDisguisedEvent event = new ClosedBlockDisguisedEvent(player, blockAt, block);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            settings.setDisguise(previousDisguise);
            ElevatorMessage.DISGUISE_FAILED.send(player);
            return true;
        }

        if (block.getSaveData().saveBlockData()) {
            ElevatorMessage.DISGUISE_SUCCESS.send(player);
            customBlock.place(blockAt.getLocation());

            plugin.getParticleAPI()
                    .playDisguiseEffect(world, blockAt.getLocation().clone().add(0.5, 0.95, 0.5));
        } else {
            settings.setDisguise(previousDisguise);
            ElevatorMessage.DISGUISE_FAILED.send(player);
        }

        return true;
    }

    private static void handleElevatorPlace(final BlockPlaceEventWrapper event, final int slot, final ItemStack item) {
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

        for (ElevatorBlock eb : elevatorsInLine) {
            if (eb.getY() < y)
                previous = eb;

            if (eb.getY() > y) {
                next = eb;
                break;
            }
        }

        boolean isClose = checkIfIsTooClose(previous, next, y);
        boolean isFar = checkIfIsTooFar(previous, next, y);

        AtomicInteger floorsInstance = (next != null ? next.getFloorAtomic() :
                previous != null ? previous.getFloorAtomic() : new AtomicInteger(1));

        if (isClose) {
            ElevatorMessage.PLACEMENT_TOO_CLOSE.send(player, MessageParameter.blocks(2));
            return;
        } else if (isFar) {
            ElevatorMessage.PLACEMENT_TOO_FAR.send(player, MessageParameter.blocks(
                    ElevatorConfig.MAX_DISTANCE.get()
            ));
            return;
        }

        ElevatorBlock eb = new ElevatorBlock(player, world, x, y, z, ElevatorConfig.DISGUISE.get(), floorsInstance, event.getPlugin());
        if (ClosedBlocksAPI.getInstance().getBlockStorage().placeBlock(eb)) {
            removeItemFromInventory(slot, item, player);
            block.setMetadata("closed_type", new FixedMetadataValue(event.getPlugin(), "elevator"));

            ClosedBlockPlacedEvent placedEvent = new ClosedBlockPlacedEvent(player, block, eb);
            Bukkit.getScheduler().runTask(event.getPlugin(), () -> {
                ElevatorMessage.PLACEMENT_SUCCESS.send(player);
                Bukkit.getPluginManager().callEvent(placedEvent);
            });
        } else {
            ElevatorMessage.PLACEMENT_FAILED.send(player);
            event.setCancelled(true);
        }
    }

    private static void removeItemFromInventory(int slot, ItemStack item, Player player) {
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            PlayerInventory inventory = player.getInventory();
            item.setAmount(item.getAmount() - 1);
            inventory.setItem(slot, item);
        }
    }

    private static boolean checkIfIsTooClose(final Elevator previous, final Elevator next, final int y) {
        if (previous != null) {
            int preY = previous.getY();
            if (Math.abs(y - preY) <= 2)
                return true;
        }
        if (next != null) {
            int nextY = next.getY();
            return Math.abs(nextY - y) <= 2;
        }

        return false;
    }

    private static boolean checkIfIsTooFar(final Elevator previous, final Elevator next, final int y) {
        if (previous != null) {
            int preY = previous.getY();
            if (Math.abs(y - preY) > ElevatorConfig.MAX_DISTANCE.get())
                return true;
        }
        if (next != null) {
            int nextY = next.getY();
            return Math.abs(nextY - y) > ElevatorConfig.MAX_DISTANCE.get();
        }

        return false;
    }
}
