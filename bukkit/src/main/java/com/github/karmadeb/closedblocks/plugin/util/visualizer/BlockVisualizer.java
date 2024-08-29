package com.github.karmadeb.closedblocks.plugin.util.visualizer;

import com.github.karmadeb.closedblocks.api.ClosedAPI;
import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
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

public class BlockVisualizer {

    private final ClosedBlocksPlugin plugin;

    private BukkitTask task;

    public BlockVisualizer(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (plugin.getParticleAPI().isInvalid())
            return;

        if (task != null && !task.isCancelled())
            return;

        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            visualizeElevators();
            visualizeMines();
        }, 0, 20 * 3);
    }

    private void visualizeElevators() {
        for (Elevator elevator : ClosedAPI.getInstance().getBlockStorage().getAllBlocks(BlockType.ELEVATOR)) {
            BlockSettings settings = elevator.getSettings();
            if (!settings.isEnabled())
                continue;

            World world = elevator.getWorld();
            Block block = world.getBlockAt(elevator.getX(), elevator.getY(), elevator.getZ());
            Location offsetLocation = block.getLocation().clone().add(0.5, 2.15, 0.5);

            Collection<? extends Player> viewer = mapBlockViewers(elevator);
            if (elevator.hasNext())
                plugin.getParticleAPI().playLineToUpEffect(offsetLocation, viewer);

            if (elevator.hasPrevious())
                plugin.getParticleAPI().playLineToDownEffect(offsetLocation, viewer);
        }
    }

    private void visualizeMines() {
        for (Mine mine : ClosedAPI.getInstance().getBlockStorage().getAllBlocks(BlockType.MINE)) {
            BlockSettings settings = mine.getSettings();
            if (!settings.isEnabled())
                continue;

            World world = mine.getWorld();
            Block block = world.getBlockAt(mine.getX(), mine.getY(), mine.getZ());
            Location offsetLocation = block.getLocation().clone().add(0.5, 1.65, 0.5);

            Collection<? extends Player> viewer = mapBlockViewers(mine);
            plugin.getParticleAPI().playMineEffect(offsetLocation, viewer, mine.causesFire(), mine.isDefused());
        }
    }

    private static @NotNull Collection<? extends Player> mapBlockViewers(final ClosedBlock block) {
        if (block.getSettings().isVisible())
            return Bukkit.getOnlinePlayers();

        Set<Player> viewer = new HashSet<>();
        OfflinePlayer owner = block.getOwner();
        if (owner.isOnline())
            viewer.add(owner.getPlayer());
        block.getSettings().getViewers().stream()
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
