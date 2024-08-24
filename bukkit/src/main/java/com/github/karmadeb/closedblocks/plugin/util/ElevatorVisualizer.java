package com.github.karmadeb.closedblocks.plugin.util;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ElevatorVisualizer {

    private final ClosedBlocksPlugin plugin;

    private BukkitTask task;

    public ElevatorVisualizer(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (plugin.getParticleAPI().isInvalid())
            return;

        if (task != null && !task.isCancelled())
            return;

        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Elevator elevator : ClosedAPI.getInstance().getBlockStorage().getAllBlocks(Elevator.class)) {
                BlockSettings settings = elevator.getSettings();
                if (!settings.isEnabled())
                    continue;

                World world = elevator.getWorld();
                Block block = world.getBlockAt(elevator.getX(), elevator.getY(), elevator.getZ());
                Location offsetLocation = block.getLocation().clone().add(0.5, 2.15, 0.5);

                Collection<? extends Player> viewer = mapElevatorViewers(elevator);
                if (elevator.hasNext())
                    plugin.getParticleAPI().playLineToUpEffect(offsetLocation, viewer);

                if (elevator.hasPrevious())
                    plugin.getParticleAPI().playLineToDownEffect(offsetLocation, viewer);
            }
        }, 0, 20 * 3);
    }

    private static @NotNull Collection<? extends Player> mapElevatorViewers(final Elevator elevator) {
        if (elevator.getSettings().isVisible())
            return Bukkit.getOnlinePlayers();

        Set<Player> viewer = new HashSet<>();
        OfflinePlayer owner = elevator.getOwner();
        if (owner.isOnline())
            viewer.add(owner.getPlayer());
        elevator.getSettings().getViewers().stream()
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .forEach(viewer::add);

        Bukkit.getOnlinePlayers().stream().filter((p) -> p.hasPermission("closedblocks.view.all"))
                .forEach(viewer::add);

        return viewer;
    }

    public void kill() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
