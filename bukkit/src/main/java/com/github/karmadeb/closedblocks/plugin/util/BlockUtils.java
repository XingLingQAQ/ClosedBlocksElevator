package com.github.karmadeb.closedblocks.plugin.util;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.event.world.mine.MineTriggeredEvent;
import com.github.karmadeb.closedblocks.api.file.configuration.mine.MineConfig;
import com.github.karmadeb.closedblocks.api.file.messages.mine.MineMessage;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockUtils {

    private static final ClosedBlocksPlugin plugin = JavaPlugin.getPlugin(ClosedBlocksPlugin.class);
    private static final Set<Mine> fuseList = ConcurrentHashMap.newKeySet();

    private BlockUtils() {

    }

    public static void explodeMine(final Mine block, final Entity entity, final MineTriggeredEvent.Reason reason, final MineExplodeContext context) {
        fuseList.add(block);

        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        Entity eTrigger = (entity == null ? (context == null ? null : context.getEntity()) : entity);
        MineTriggeredEvent.Reason rTrigger = context == null ? reason : MineTriggeredEvent.Reason.CHAIN_REACTION;

        MineTriggeredEvent event = new MineTriggeredEvent(eTrigger, block, rTrigger, (context == null ? null : context.getEvent()));
        MineExplodeContext newContext = new MineExplodeContext(event.getEntity(), event);

        Block bBlock = world.getBlockAt(x, y, z);
        Collection<Mine> affectedBlocks = (MineConfig.CHAIN_EXPLOSION.get() ? getBlocksInRadius(world, bBlock.getLocation(), (int) block.getPower()) :
                Collections.emptyList());

        bBlock.removeMetadata("closed_type", plugin);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            world.createExplosion(x, y, z, block.getPower(), block.causesFire(), false);
            Bukkit.getPluginManager().callEvent(event);
            fuseList.remove(block);

            int await = 0;
            for (Mine affected : affectedBlocks) {
                ClosedAPI.getInstance().getBlockStorage().destroyBlock(affected);
                Bukkit.getScheduler().runTaskLater(plugin, () -> explodeMine(affected, entity, MineTriggeredEvent.Reason.CHAIN_REACTION, newContext), await++);
            }
        }, 10);
    }

    @SuppressWarnings("t")
    public static void rightClickMine(final int slot, final ItemStack item, final Mine mine, final Player player) {
        if (item.getType().equals(Material.BLAZE_ROD)) {
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

    @SuppressWarnings("t")
    private static Collection<Mine> getBlocksInRadius(final World world, final Location origin, final int radius) {
        Collection<Mine> blocks = new HashSet<>();

        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        int radiusSquared = radius * radius;

        for (int x = originX - radius; x <= originX + radius; x++) {
            for (int y = originY - radius; y <= originY + radius; y++) {
                for (int z = originZ - radius; z <= originZ + radius; z++) {
                    Location loc = new Location(world, x, y, z);
                    if (origin.distanceSquared(loc) <= radiusSquared) {
                        Block block = loc.getBlock();
                        if (!ClosedAPI.getInstance().getBlockStorage().isClosedBlock(block)) continue;

                        ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
                        if (!(cb instanceof Mine)) continue;

                        Mine mine = (Mine) cb;
                        if (fuseList.contains(mine) || mine.isDefused() || !mine.getSettings().isEnabled()) continue;

                        blocks.add(mine);
                    }
                }
            }
        }

        return blocks;
    }

    public static class MineExplodeContext {

        private final Entity entity;
        private final MineTriggeredEvent event;

        public MineExplodeContext(final Entity entity, final MineTriggeredEvent event) {
            this.entity = entity;
            this.event = event;
        }

        public Entity getEntity() {
            return this.entity;
        }

        public MineTriggeredEvent getEvent() {
            return this.event;
        }
    }
}