package com.github.karmadeb.closedblocks.plugin.integrations.shared;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.file.messages.elevator.ElevatorMessage;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class IntegrationUtils {

    public static void removeElevator(final ClosedBlocksPlugin plugin, final Player player, final ClosedBlock block, final ItemStack elevatorItem) {
        if (elevatorItem == null) {
            plugin.getLogger().severe("Failed to obtain an elevator item");
            ElevatorMessage.DESTROY_FAILED.send(player);
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            Map<Integer, ItemStack> fitResult = inventory.addItem(elevatorItem);
            if (!fitResult.isEmpty()) {
                ElevatorMessage.DESTROY_INVENTORY_FULL.send(player);
                return;
            }
        }

        if (!ClosedBlocksAPI.getInstance().getBlockStorage().destroyBlock(block)) {
            if (!player.getGameMode().equals(GameMode.CREATIVE))
                inventory.remove(elevatorItem);

            ElevatorMessage.DESTROY_FAILED.send(player);
            return;
        }

        ElevatorMessage.DESTROY_SUCCESS.send(player);
    }
}
