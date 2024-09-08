package com.github.karmadeb.closedblocks.plugin.integrations.discordsrv.events;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.data.BlockSettings;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockDisguisedEvent;
import com.github.karmadeb.closedblocks.api.event.world.ClosedBlockPlacedEvent;
import com.github.karmadeb.closedblocks.api.event.world.mine.MineTriggeredEvent;
import com.github.karmadeb.closedblocks.api.file.configuration.plugin.PluginConfig;
import com.github.karmadeb.closedblocks.plugin.integrations.discordsrv.DiscordSRVIntegration;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;

public class DClosedBlockListener implements Listener {

    private final DiscordSRVIntegration integration;

    public DClosedBlockListener(final DiscordSRVIntegration integration) {
        this.integration = integration;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaced(ClosedBlockPlacedEvent e) {
        ClosedBlock block = e.getPlacedBlock();
        if (block.getType().equals(BlockType.ELEVATOR) && !PluginConfig.LOG_ELEVATOR_PLACE.get())
            return;
        if (block.getType().equals(BlockType.MINE) && !PluginConfig.LOG_MINE_PLACE.get())
            return;

        MessageEmbed embed = createBlockPlaceEmbed(e);
        integration.async(() -> {
            TextChannel channel = integration.getClosedBlocksChannel();
            channel.sendMessageEmbeds(embed).complete();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDisguised(ClosedBlockDisguisedEvent e) {
        ClosedBlock block = e.getDisguisedBlock();
        if (block.getType().equals(BlockType.ELEVATOR) && !PluginConfig.LOG_ELEVATOR_DISGUISE.get())
            return;

        MessageEmbed embed = createBlockDisguisedEmbed(e);
        integration.async(() -> {
            TextChannel channel = integration.getClosedBlocksChannel();
            channel.sendMessageEmbeds(embed).complete();
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onMineTriggered(MineTriggeredEvent e) {
        if (!PluginConfig.LOG_MINE_TRIGGER_ONCE.get()) return;
        if (e.isChained() && !PluginConfig.LOG_MINE_TRIGGER_CHAIN.get()) return;

        MessageEmbed embed = createMineTriggerEmbed(e);
        integration.async(() -> {
            TextChannel channel = integration.getClosedBlocksChannel();
            channel.sendMessageEmbeds(embed).complete();
        });
    }

    private MessageEmbed createBlockPlaceEmbed(final ClosedBlockPlacedEvent e) {
        Player player = e.getPlayer();
        ClosedBlock block = e.getPlacedBlock();
        BlockSettings settings = block.getSettings();
        World world = block.getWorld();
        String x = String.valueOf(block.getX());
        String y = String.valueOf(block.getY());
        String z = String.valueOf(block.getZ());

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("ClosedBlocks - Block update")
                .setDescription("A new block has been placed by a player")
                .setColor(Color.BLUE)
                .addBlankField(false)
                .addField("Player", player.getName(), false)
                .addField("World", world.getName(), false)
                .addField("X", x, true)
                .addField("Y", y, true)
                .addField("Z", z, true)
                .addField("Type", block.getType().singular(), true)
                .addField("Disguise", settings.getDisguiseName(), true);

        if (block instanceof Mine) {
            Mine mine = (Mine) block;
            builder.addBlankField(false);
            builder.addField("Power", String.valueOf(mine.getPower()), true);
            builder.addField("Incendiary", String.valueOf(mine.causesFire()), true);
            builder.addField("Defused", String.valueOf(mine.isDefused()), true);
        }

        return builder.build();
    }

    private MessageEmbed createBlockDisguisedEmbed(final ClosedBlockDisguisedEvent e) {
        Player player = e.getPlayer();
        ClosedBlock block = e.getDisguisedBlock();
        BlockSettings settings = block.getSettings();
        World world = block.getWorld();
        String x = String.valueOf(block.getX());
        String y = String.valueOf(block.getY());
        String z = String.valueOf(block.getZ());

        return new EmbedBuilder()
                .setTitle("ClosedBlocks - Block update")
                .setDescription("A block has been disguised")
                .setColor(Color.BLUE)
                .addBlankField(false)
                .addField("Player", player.getName(), false)
                .addField("World", world.getName(), false)
                .addField("X", x, true)
                .addField("Y", y, true)
                .addField("Z", z, true)
                .addField("Type", block.getType().singular(), true)
                .addField("Disguise", settings.getDisguiseName(), true)
                .addField("Previous", e.getPreviousDisguiseName(), true)
                .build();
    }

    private MessageEmbed createMineTriggerEmbed(final MineTriggeredEvent e) {
        Entity entity = e.getEntity();
        Mine block = e.getMine();
        World world = block.getWorld();
        String x = String.valueOf(block.getX());
        String y = String.valueOf(block.getY());
        String z = String.valueOf(block.getZ());

        String description = "A mine has been triggered";
        if (e.isChained()) {
            MineTriggeredEvent chain = e.getChained();

            Mine pre = chain.getMine();
            description += " triggered by a mine at X: `" + pre.getX() + "` | Y: `" + pre.getY() + "` | Z: `" + pre.getZ() + "`";

            MineTriggeredEvent.Reason originalReason;
            do {
                originalReason = chain.getReason();
                chain = chain.getChained();
            } while (chain != null);

            description += " caused by: `" + originalReason.getName() + "`";
        }

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("ClosedBlocks - Block destroyed")
                .setDescription(description)
                .setColor(Color.BLUE)
                .addBlankField(false);

        if (entity != null) {
            builder.addField("Entity", entity.getName(), false);
        }

        builder.addField("World", world.getName(), false)
                .addField("X", x, true)
                .addField("Y", y, true)
                .addField("Z", z, true)
                .addField("Power", String.valueOf(block.getPower()), true)
                .addField("Incendiary", String.valueOf(block.causesFire()), true)
                .addField("Reason", e.getReason().getName(), true)
                .build();

        return builder.build();
    }
}
