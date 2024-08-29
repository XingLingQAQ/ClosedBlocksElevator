package com.github.karmadeb.closedblocks.plugin.util;

import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.file.configuration.mine.MineConfig;
import com.github.karmadeb.closedblocks.api.file.messages.mine.MineMessage;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockUtils {

    private static final ClosedBlocksPlugin plugin = JavaPlugin.getPlugin(ClosedBlocksPlugin.class);

    private BlockUtils() {

    }

    public static void explodeMine(final Mine block) {
        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                world.createExplosion(x, y, z, block.getPower(), block.causesFire(), true), 10);
    }

    @SuppressWarnings("t")
    public static void rightClickMine(final int slot, final ItemStack item, final Mine mine, final Player player) {
        if (item.getType().equals(Material.FLINT_AND_STEEL)) {
            if (mine.causesFire()) {
                MineMessage.ALREADY_INCENDIARY.send(player);
            } else {
                mine.setCausesFire(true);
                if (!mine.getSaveData().saveBlockData()) {
                    mine.setCausesFire(false);
                    return;
                }

                Damageable meta = (Damageable) item.getItemMeta();
                assert meta != null;

                meta.setDamage(meta.getDamage() - 1);
                item.setItemMeta(meta);

                player.getInventory().setItem(slot, item);
                MineMessage.MINE_INCENDIARY.send(player);
            }

            return;
        } else {
            if (mine.isDefused()) {
                mine.setDefused(false);
                if (!mine.getSaveData().saveBlockData()) {
                    mine.setDefused(true);
                    return;
                }

                MineMessage.MINE_FUSED.send(player);
                return;
            } else {
                float power = mine.getPower();
                if (power >= MineConfig.MAX_POWER.get().floatValue()) {
                    MineMessage.POWER_CANNOT_INCREASE.send(player);
                    return;
                } else {
                    mine.setPower(Math.min(MineConfig.MAX_POWER.get().floatValue(), power + MineConfig.POWER.get().floatValue()));
                    if (!mine.getSaveData().saveBlockData()) {
                        mine.setPower(power);
                        return;
                    }
                }
            }
        }

        item.setAmount(item.getAmount() - 1);
        player.getInventory().setItem(slot, item);
        MineMessage.POWER_INCREASED.send(player);
    }
}
