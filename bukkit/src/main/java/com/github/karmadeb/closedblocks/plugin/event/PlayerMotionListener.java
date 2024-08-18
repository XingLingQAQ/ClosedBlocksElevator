package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.util.NullableChain;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMotionListener implements Listener {

    private final ClosedBlocksPlugin plugin;
    private final Map<UUID, BukkitTask> denyUpLevel = new ConcurrentHashMap<>();

    private final Sound upSound = NullableChain.of(() -> tryGetSound("UI_TOAST_IN"))
            .or(() -> tryGetSound("TOAST_IN"))
            .or(() -> tryGetSound("ENTITY_GHAST_SHOOT"))
            .or(() -> tryGetSound("GHAST_SHOOT"))
            .or(() -> tryGetSound("ENTITY_GHAST_FIREBALL"))
            .or(() -> tryGetSound("GHAST_FIREBALL"))
            .orElse(Sound.values()[0]);
    private final Sound downSound = NullableChain.of(() -> tryGetSound("UI_TOAST_OUT"))
            .or(() -> tryGetSound("TOAST_OUT"))
            .or(() -> tryGetSound("ENTITY_BAT_TAKEOFF"))
            .or(() -> tryGetSound("BAT_TAKEOFF"))
            .orElse(Sound.values()[1]);
    private final Sound plingSound = NullableChain.of(() -> tryGetSound("ENTITY_EXPERIENCE_ORB_PICKUP"))
            .or(() -> tryGetSound("EXPERIENCE_ORB_PICKUP"))
            .or(() -> tryGetSound("ORB_PICKUP"))
            .or(() -> tryGetSound("NOTE_PLING"))
            .orElse(Sound.values()[2]);

    public PlayerMotionListener(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCrouch(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        Block block = player.getLocation().getBlock();
        if (!block.hasMetadata("closed_type"))
            block = block.getRelative(BlockFace.DOWN);

        if (player.isSneaking() || !block.hasMetadata("closed_type"))
            return;
        String type = getClosedType(block);
        if (type == null || !type.equals("elevator"))
            return;

        Elevator cb = (Elevator) ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
        if (cb == null)
            return;

        if (cb.hasPrevious()) {
            Elevator pre = cb.getPrevious().orElse(null);
            useElevator(pre, player, downSound);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJump(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.isFlying() || player.isSneaking() || denyUpLevel.containsKey(player.getUniqueId()))
            return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (to == null)
            return;

        if (from.getY() < to.getY())
            handleJump(e);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlyToggle(PlayerToggleFlightEvent e) {
        Player player = e.getPlayer();
        if (denyUpLevel.containsKey(player.getUniqueId())) {
            denyUpLevel.remove(player.getUniqueId()).cancel();

            Location vTo = player.getLocation().clone().add(0, 0.25, 0);
            PlayerMoveEvent vEvent = new PlayerMoveEvent(player, player.getLocation(), vTo);

            handleJump(vEvent);
            if (vEvent.getTo() != null && !vEvent.getTo().equals(vTo))
                player.teleport(vEvent.getTo());

            e.setCancelled(true);
        }
    }

    private void handleJump(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Block block = player.getLocation().getBlock();
        if (!block.hasMetadata("closed_type"))
            block = block.getRelative(BlockFace.DOWN);

        String type = getClosedType(block);
        if (type == null || !type.equals("elevator"))
            return;

        Elevator cb = (Elevator) ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
        if (cb == null)
            return;

        if (cb.hasNext()) {
            Elevator next = cb.getNext().orElse(null);
            useElevator(next, player, upSound);
        }
    }

    private void useElevator(final Elevator elevator, final Player player, final Sound useSound) {
        if (elevator == null) return;

        Location newLocation = player.getLocation().clone();
        newLocation.setX(elevator.getX() + 0.5);
        newLocation.setY(elevator.getY() + 1.15);
        newLocation.setZ(elevator.getZ() + 0.5);

        player.teleport(newLocation);
        playElevatorUseSound(player, useSound, elevator.getLevel());
    }

    private void playElevatorUseSound(final Player player, final Sound upSound, final int level) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&3Elevator level:"),
                ChatColor.translateAlternateColorCodes('&', "&e" + level),
                0, 20 * 3, 0);
        player.playSound(player.getLocation(), upSound, 2f, 2f);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            denyUpLevel.remove(player.getUniqueId());
            player.playSound(player.getLocation(), plingSound, 2f, 0f);
        }, 5);
        denyUpLevel.put(player.getUniqueId(), task);
    }

    private String getClosedType(final Block block) {
        List<MetadataValue> values = block.getMetadata("closed_type");
        for (MetadataValue value : values) {
            try {
                return value.asString();
            } catch (Throwable ignored) {}
        }

        return null;
    }

    private Sound tryGetSound(final String name) {
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException ignored) {}
        return null;
    }
}
