package com.github.karmadeb.closedblocks.plugin.integrations.shared.wrapper;

import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class BlockBreakEventWrapper {

    private final ClosedBlocksPlugin plugin;
    private final Cancellable event;
    private final Player player;
    private final Block block;

    public BlockBreakEventWrapper(final ClosedBlocksPlugin plugin, final Cancellable event, final Player player, final Block block) {
        this.plugin = plugin;
        this.event = event;
        this.player = player;
        this.block = block;
    }

    public ClosedBlocksPlugin getPlugin() {
        return this.plugin;
    }

    public Player getPlayer() {
        return this.player;
    }

    public Block getBlock() {
        return this.block;
    }

    public void setCancelled(final boolean cancelled) {
        this.event.setCancelled(cancelled);
    }
}
