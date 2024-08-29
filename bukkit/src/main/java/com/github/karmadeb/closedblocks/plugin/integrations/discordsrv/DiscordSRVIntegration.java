package com.github.karmadeb.closedblocks.plugin.integrations.discordsrv;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.api.item.ItemType;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.discordsrv.events.DClosedBlockListener;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.JDA;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.awt.*;
import java.util.Map;

public class DiscordSRVIntegration implements Integration {

    private final ClosedBlocksPlugin plugin;

    private TextChannel closedBlocksChannel;
    private DClosedBlockListener blockListener;

    public DiscordSRVIntegration(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the integration name
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "DiscordSRV";
    }

    /**
     * Load the integration
     */
    @Override
    public void load() {
        DiscordSRV.api.subscribe(this);
    }

    /**
     * Unload the integration
     */
    @Override
    public void unload() {
        if (blockListener != null)
            HandlerList.unregisterAll(blockListener);
    }

    @Subscribe
    public void discordReadyEvent(DiscordReadyEvent event) {
        Map<String, String> channels = DiscordSRV.config().getMap("Channels");
        String closedBlocksId = channels.get("closedblocks");
        if (closedBlocksId == null) {
            this.plugin.getLogger().warning("Missing channel configuration for ClosedBlocks. Please add \"closedblocks\": \"channel id\" to DiscordSRV Channels setting");
            return;
        }

        JDA jda = DiscordUtil.getJda();

        closedBlocksChannel = jda.getTextChannelById(closedBlocksId);
        if (closedBlocksChannel == null || !closedBlocksChannel.canTalk()) {
            this.plugin.getLogger().warning("Invalid closedblocks channel id. Please make sure the channel id is correct and that the bot can talk on it");
            return;
        }

        MessageEmbed message = new EmbedBuilder()
                .setTitle("ClosedBlocks")
                .setDescription("Plugin is now hooked into DiscordSRV")
                .setAuthor("~ KarmaDev")
                .setColor(Color.BLUE)
                .build();

        closedBlocksChannel.sendMessageEmbeds(message).complete();
        blockListener = new DClosedBlockListener(this);

        Bukkit.getPluginManager().registerEvents(blockListener, plugin);
    }

    /**
     * Get if the integration is supported
     *
     * @return if the integration is
     * supported by the plugin
     */
    @Override
    public boolean isSupported() {
        return Bukkit.getPluginManager().isPluginEnabled("DiscordSRV");
    }

    public TextChannel getClosedBlocksChannel() {
        return this.closedBlocksChannel;
    }

    public void async(final Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, task);
    }

    public void grantBlockEmbed(final CommandSender who, final Player to, final BlockType<?> type, final int amount) {
        if (!this.isSupported()) return;
        this.async(() -> grantEmbed(who, to, type.singular(), "block(s)", amount));
    }

    public void grantItemEmbed(final CommandSender who, final Player to, final ItemType type, final int amount) {
        if (!this.isSupported()) return;
        this.async(() -> grantEmbed(who, to, type.name(), "item(s)", amount));
    }

    private void grantEmbed(final CommandSender who, final Player to, final String typeName, final String type, final int amount) {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("ClosedBlocks - Granted")
                .setDescription(String.format("%s granted x%d %s %s to %s",
                        who.getName(),
                        amount,
                        typeName,
                        type,
                        to.getName()))
                .setColor(Color.BLUE)
                .build();

        closedBlocksChannel.sendMessageEmbeds(embed).complete();
    }
}
