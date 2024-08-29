package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.item.mine.MineDiffuser;
import com.github.karmadeb.closedblocks.api.util.NullableChain;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.IntegrationUtils;
import com.github.karmadeb.closedblocks.plugin.util.BlockUtils;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.github.karmadeb.closedblocks.plugin.util.SoundUtils.tryGetSound;

public class BlockInteractionListener implements Listener {

    private final ClosedBlocksPlugin plugin;
    private final Map<UUID, Long> lastInteraction = new ConcurrentHashMap<>();

    private final Sound cutSound = NullableChain.of(() -> tryGetSound("BLOCK_BEEHIVE_SHEAR"))
            .or(() -> tryGetSound("BEEHIVE_SHEAR"))
            .or(() -> tryGetSound("BLOCK_SHEAR"))
            .or(() -> tryGetSound("SHEAR"))
            .orElse(Sound.values()[6]);

    public BlockInteractionListener(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true) @SuppressWarnings("t")
    public void onHandInteract(PlayerInteractEvent e) {
        long now = System.currentTimeMillis();

        Player player = e.getPlayer();
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack used = player.getInventory().getItem(slot);
        if (used == null || used.getType().equals(Material.AIR)) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        boolean isDiffuser = NBT.get(used, (Function<ReadableItemNBT, Boolean>) (data) -> data.getString("closed_type").equals("mine_diffuser"));
        if (isDiffuser) {
            if (lastInteraction.containsKey(player.getUniqueId()) && lastInteraction.get(player.getUniqueId()) >= now) {
                e.setCancelled(true);
                return;
            }

            lastInteraction.put(player.getUniqueId(), now + TimeUnit.SECONDS.toMillis(1));
            MineDiffuser diffuser = new MineDiffuser(used);
            diffuser.use();
            diffuser.save();

            used = diffuser.getItem();
            if (diffuser.getUsages() == 0) {
                used.setAmount(used.getAmount() - 1);
                player.getInventory().setItem(slot, used);
            } else {
                player.getInventory().setItem(slot, used);
            }

            e.setCancelled(true);
        }

        if (!ClosedAPI.getInstance().getBlockStorage().isClosedBlock(clicked)) return;

        ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(clicked).orElse(null);
        if (cb == null) return;

        if (cb.getType().equals(BlockType.MINE)) {
            Mine mine = (Mine) cb;

            if (isDiffuser) {
                e.setCancelled(true);
                if (mine.isDefused()) return;

                mine.setDefused(true);
                if (!mine.getSaveData().saveBlockData()) {
                    mine.setDefused(false);
                    return;
                }

                player.playSound(clicked.getLocation(), cutSound, 2f, 2f);
            } else if (used.getType().equals(Material.BLAZE_ROD)) {
                if (lastInteraction.containsKey(player.getUniqueId()) && lastInteraction.get(player.getUniqueId()) >= now) {
                    e.setCancelled(true);
                    return;
                }

                lastInteraction.put(player.getUniqueId(), now + TimeUnit.SECONDS.toMillis(1));

                e.setCancelled(true);
                BlockUtils.rightClickMine(slot, used, mine, player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking() || !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        ItemStack used = e.getItem();
        if (used != null && !used.getType().equals(Material.AIR)) return;

        Block clicked = e.getClickedBlock();
        if (clicked == null) return;

        if (!ClosedAPI.getInstance().getBlockStorage().isClosedBlock(clicked)) return;

        ClosedBlock cb = ClosedAPI.getInstance().getBlockStorage().getFromBlock(clicked).orElse(null);
        if (cb == null) return;

        e.setCancelled(true);
        OfflinePlayer owner = cb.getOwner();
        if (!owner.getUniqueId().equals(player.getUniqueId()))
            return;

        IntegrationUtils.removeClosedBlock(plugin, player, cb, ClosedAPI.createItem(cb.getType()));
        if (!(cb instanceof Mine))
            clicked.setType(Material.AIR);

        plugin.getParticleAPI().playDisguiseEffect(clicked.getWorld(), clicked.getLocation().clone().add(0.5, 0.95, 0.5));
    }
}
