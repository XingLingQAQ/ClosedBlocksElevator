package com.github.karmadeb.closedblocks.plugin.integrations.shared;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.file.configuration.elevator.ElevatorConfig;
import com.github.karmadeb.closedblocks.api.file.messages.PluginMessages;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public class IntegrationUtils {

    public static void removeClosedBlock(final ClosedBlocksPlugin plugin, final Player player, final ClosedBlock block, final ItemStack elevatorItem) {
        if (elevatorItem == null) {
            plugin.getLogger().severe("Failed to obtain an elevator item");
            PluginMessages.DESTROY_FAILED.send(player, MessageParameter.type(block));
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            Map<Integer, ItemStack> fitResult = inventory.addItem(elevatorItem);
            if (!fitResult.isEmpty()) {
                PluginMessages.DESTROY_INVENTORY_FULL.send(player, MessageParameter.type(block));
                return;
            }
        }

        if (!ClosedBlocksAPI.getInstance().getBlockStorage().destroyBlock(block)) {
            if (!player.getGameMode().equals(GameMode.CREATIVE))
                inventory.remove(elevatorItem);

            PluginMessages.DESTROY_FAILED.send(player, MessageParameter.type(block));
            return;
        }

        PluginMessages.DESTROY_SUCCESS.send(player, MessageParameter.type(block));
    }

    public static boolean isIllegalElevatorType(final Material material) {
        if (invalidElevatorItself(material)) return true;
        String name = material.name();

        return name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR") || name.endsWith("_BUTTON") ||
                name.endsWith("_SHULKER_BOX") || name.endsWith("_BANNER") || name.endsWith("_WALL_HANGING_SIGN") ||
                name.endsWith("HANGING_SIGN") || name.endsWith("_SIGN");
    }

    private static boolean invalidElevatorItself(Material material) {
        if (material == null)
            return true;

        return !material.isBlock() || !material.isSolid() || ElevatorConfig.DISGUISE_BLACKLIST.get().contains(material.name()) ||
                material.isInteractable() || material.equals(Material.TNT) ||
                material.equals(Material.LEVER) || material.equals(Material.SHULKER_BOX);
    }
}
