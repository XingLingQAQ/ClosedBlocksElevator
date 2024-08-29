package com.github.karmadeb.closedblocks.plugin.loader;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.utils.ParticleException;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.command.ClosedBlockCommand;
import com.github.karmadeb.closedblocks.plugin.event.BlockGriefListener;
import com.github.karmadeb.closedblocks.plugin.event.BlockInteractionListener;
import com.github.karmadeb.closedblocks.plugin.event.PlayerMotionListener;
import com.github.karmadeb.closedblocks.plugin.util.visualizer.BlockVisualizer;
import com.github.karmadeb.closedblocks.plugin.util.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;

import java.lang.reflect.Field;
import java.util.logging.Level;

public final class BukkitLoader {

    private final ClosedBlocksPlugin plugin;

    private BlockInteractionListener interactionListener;
    private PlayerMotionListener motionListener;
    private BlockGriefListener griefListener;

    private PluginCommand closedBlocksCommand;

    private ParticleUtils particleUtils;
    private BlockVisualizer blockVisualizer;

    public BukkitLoader(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents() {
        interactionListener = new BlockInteractionListener(this.plugin);
        motionListener = new PlayerMotionListener(this.plugin);
        griefListener = new BlockGriefListener();

        Bukkit.getServer().getPluginManager().registerEvents(interactionListener, this.plugin);
        Bukkit.getServer().getPluginManager().registerEvents(motionListener, this.plugin);
        Bukkit.getServer().getPluginManager().registerEvents(griefListener, this.plugin);
    }

    public void registerCommands() {
        closedBlocksCommand = this.plugin.getCommand("closedblocks");
        if (closedBlocksCommand == null) {
            this.plugin.getLogger().severe("Invalid or corrupt plugin.yml. Missing closedblocks command, disabling plugin...");
            this.plugin.getPluginLoader().disablePlugin(this.plugin);
        }

        ClosedBlockCommand executor = new ClosedBlockCommand(this.plugin);
        closedBlocksCommand.setExecutor(executor);
        closedBlocksCommand.setTabCompleter(executor);
    }

    public void setupParticleAPI() {
        ParticleNativeAPI pna = null;
        try {
            pna = ParticleNativeCore.loadAPI(this.plugin);
        } catch (ParticleException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Failed to enable particle API. Particles won't be displayed", ex);
        }

        particleUtils = new ParticleUtils(this.plugin, pna);
    }

    public void createBlockVisualizer() {
        this.blockVisualizer = new BlockVisualizer(this.plugin);
        this.blockVisualizer.start();
    }

    public ParticleUtils getParticleUtils() {
        return this.particleUtils;
    }

    public void shutdown() {
        HandlerList.unregisterAll(this.interactionListener);
        HandlerList.unregisterAll(this.motionListener);
        HandlerList.unregisterAll(this.griefListener);

        CommandMap map = this.getBukkitCommandMap();
        this.closedBlocksCommand.unregister(map);

        this.blockVisualizer.kill();
    }

    private CommandMap getBukkitCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);

            return (CommandMap) field.get(Bukkit.getServer());
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }
}