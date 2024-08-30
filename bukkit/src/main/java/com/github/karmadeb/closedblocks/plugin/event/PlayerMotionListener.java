package com.github.karmadeb.closedblocks.plugin.event;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.event.world.elevator.ElevatorUsedEvent;
import com.github.karmadeb.closedblocks.api.event.world.mine.MineTriggeredEvent;
import com.github.karmadeb.closedblocks.api.file.configuration.mine.MineConfig;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.api.file.messages.elevator.ElevatorMessage;
import com.github.karmadeb.closedblocks.api.util.NullableChain;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksAPI;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.karmadeb.closedblocks.plugin.util.SoundUtils.tryGetSound;

public class PlayerMotionListener implements Listener {

    private final ClosedBlocksPlugin plugin;
    private final Map<UUID, BukkitTask> denyUpLevel = new ConcurrentHashMap<>();
    private final Map<UUID, StepData> steppedMine = new ConcurrentHashMap<>();

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
    private final Sound stepInSound = NullableChain.of(() -> tryGetSound("BLOCK_METAL_PRESSURE_PLATE_CLICK_ON"))
            .or(() -> tryGetSound("METAL_PRESSURE_PLATE_CLICK_ON"))
            .or(() -> tryGetSound("PRESSURE_PLATE_CLICK_ON"))
            .orElse(Sound.values()[4]);
    private final Sound stepOutSound = NullableChain.of(() -> tryGetSound("BLOCK_METAL_PRESSURE_PLATE_CLICK_OFF"))
            .or(() -> tryGetSound("METAL_PRESSURE_PLATE_CLICK_OFF"))
            .or(() -> tryGetSound("PRESSURE_PLATE_CLICK_OFF"))
            .orElse(Sound.values()[5]);


    public PlayerMotionListener(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        this.plugin.getChecker().noticePlayer(player);
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
        if (cb == null || !cb.getSettings().isEnabled())
            return;

        if (cb.hasPrevious()) {
            Elevator pre = cb.getPrevious().orElse(null);
            useElevator(cb, pre, player, ElevatorMessage.FLOOR_DOWN_TITLE, ElevatorMessage.FLOOR_DOWN_SUBTITLE, downSound);
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

        if (from.getY() < to.getY()) {
            handleJump(e);
            return;
        }

        handleStep(to, player);
    }

    @SuppressWarnings("t")
    private void handleStep(final Location to, final Player player) {
        Block block = to.getBlock();
        if (!ClosedAPI.getInstance().getBlockStorage().isClosedBlock(to.getBlock()))
            block = block.getRelative(BlockFace.DOWN);

        StepData step = this.steppedMine.get(player.getUniqueId());
        if (step != null) {
            Block stepBlock = step.block;
            Mine mine = step.mine;

            if (!stepBlock.equals(block)) {
                handleMineStep(player, mine, stepBlock);
                return;
            }

            return;
        }

        if (ClosedAPI.getInstance().getBlockStorage().isClosedBlock(block)) {
            String type = getClosedType(block);
            if (type == null || !type.equals("mine"))
                return;

            Mine cb = (Mine) ClosedBlocksAPI.getInstance().getBlockStorage().getFromBlock(block).orElse(null);
            if (player.isSneaking() || cb == null || !cb.getSettings().isEnabled() || cb.isDefused())
                return;

            if (!MineConfig.OWNER_TRIGGER.get() && cb.getOwner().getUniqueId().equals(player.getUniqueId())) return;

            StepData data = new StepData();
            this.steppedMine.put(player.getUniqueId(), data);

            player.getWorld().playSound(block.getLocation(), stepInSound, 2f, 2f);
            data.block = block;
            data.mine = cb;
        }
    }

    private void handleMineStep(final Player player, final Mine cb, final Block block) {
        this.steppedMine.remove(player.getUniqueId());

        if (!ClosedAPI.getInstance().getBlockStorage().destroyBlock(cb))
            return;

        player.getWorld().playSound(block.getLocation(), stepOutSound, 2f, 0f);
        BlockUtils.explodeMine(cb, player, MineTriggeredEvent.Reason.STEP, null);
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
        if (cb == null || !cb.getSettings().isEnabled())
            return;

        if (cb.hasNext()) {
            Elevator next = cb.getNext().orElse(null);
            useElevator(cb, next, player, ElevatorMessage.FLOOR_UP_TITLE, ElevatorMessage.FLOOR_UP_SUBTITLE, upSound);
        }
    }

    private void useElevator(final Elevator current, final Elevator elevator, final Player player,
                             final ElevatorMessage title, final ElevatorMessage subtitle, final Sound useSound) {
        if (elevator == null) return;

        ElevatorUsedEvent event = new ElevatorUsedEvent(player, current, elevator);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        Location newLocation = player.getLocation().clone();
        newLocation.setX(elevator.getX() + 0.5);
        newLocation.setY(elevator.getY() + 1.15);
        newLocation.setZ(elevator.getZ() + 0.5);

        player.teleport(newLocation);
        playElevatorUseSound(player, title, subtitle, useSound, elevator, Math.abs(elevator.getY() - current.getY()));
    }

    private void playElevatorUseSound(final Player player, final ElevatorMessage title, final ElevatorMessage subtitle, final Sound playSound,
                                      final Elevator elevator, final int blocks) {
        player.sendTitle(title.parse(MessageParameter.floor(elevator), MessageParameter.floors(elevator), MessageParameter.blocks(blocks)),
                subtitle.parse(MessageParameter.floor(elevator), MessageParameter.floors(elevator), MessageParameter.blocks(blocks)),
                0, 20 * 3, 0);
        player.playSound(player.getLocation(), playSound, 2f, 2f);

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

    private static class StepData {
        public Mine mine;
        public Block block;
    }
}
