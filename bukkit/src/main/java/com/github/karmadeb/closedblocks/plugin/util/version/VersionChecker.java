package com.github.karmadeb.closedblocks.plugin.util.version;

import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.google.gson.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public final class VersionChecker {

    private static final String DOWNLOAD_AGENT_SPIGOTMC = "%%__RESOURCE__%%";
    private static final String DOWNLOAD_AGENT_BUILTBYBIT = "%%__VERSION__%%";
    private static final String DOWNLOAD_AGENT_POLYMART = "%%__POLYMART__%%";

    private static final String CHECK_URL = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=105696";
    private static final String CHANGELOG_URL = "https://api.spigotmc.org/simple/0.2/index.php?action=getResourceUpdates&id=105696&page=%d";

    private final ClosedBlocksPlugin plugin;

    private BaseComponent messageComponent;

    private BukkitTask checkTask;
    private boolean requiresUpdate = false;
    private String currentVersion;
    private int updateId;
    private String changelog;
    private String rawChangelog;

    public VersionChecker(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (checkTask != null)
            return;

        checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
            this.check();
            if (this.requiresUpdate) {
                checkTask.cancel();
                Bukkit.getOnlinePlayers().forEach(this::noticePlayer);

                Bukkit.getConsoleSender().sendMessage(color("&6A new version for ClosedBlocks has been released&8 (&7" + this.currentVersion + "&8)&r\n"));
                Bukkit.getConsoleSender().sendMessage(color(this.changelog));
                if (isPolymartAgent()) {
                    Bukkit.getConsoleSender().sendMessage(color("&6&lDownload the latest version now from: &7https://polymart.org/resource/3148/&r"));
                } else if (isBuiltByBitAgent()) {
                    Bukkit.getConsoleSender().sendMessage(color("&6&lDownload the latest version now from: &7https://builtbybit.com/resources/26659/&r"));
                } else if (isSpigotMCAgent()) {
                    Bukkit.getConsoleSender().sendMessage(color("&6&lDownload the latest version now from: &7https://www.spigotmc.org/resources/105696/&r"));
                } else {
                    Bukkit.getConsoleSender().sendMessage(color("&6&lGet and compile the updated source code now from: &7https://github.com/KarmaDeb/ClosedBlocksElevator&r"));
                }
            }
        }, 0, 20 * 300);
    }

    public void stop() {
        if (this.checkTask != null && !this.checkTask.isCancelled()) {
            this.checkTask.cancel();
            this.checkTask = null;
        }
    }

    public void noticePlayer(final Player player) {
        if (!this.requiresUpdate)
            return;

        if (messageComponent == null) {
            messageComponent = TextComponent.fromLegacy(color("&6There's a new version available for ClosedBlocks&8 (&7" + this.currentVersion + "&8)"));

            BaseComponent changelogComponent = TextComponent.fromLegacy(color(this.rawChangelog));
            changelogComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://www.spigotmc.org/resources/105696/update?update=" + this.updateId));

            BaseComponent updateClick;
            BaseComponent spigot = TextComponent.fromLegacy(color("&7&nSpigotMC"));
            BaseComponent builtByBit = TextComponent.fromLegacy(color("&7&nBuiltByBit"));
            BaseComponent polymart = TextComponent.fromLegacy(color("&7&nPolymart"));
            BaseComponent github = TextComponent.fromLegacy(color("&7&nGitHub"));

            spigot.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://www.spigotmc.org/resources/105696/"));
            builtByBit.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://builtbybit.com/resources/26659/"));
            polymart.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://polymart.org/resource/3148/"));
            github.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://github.com/KarmaDeb/ClosedBlocksElevator"));

            if (isPolymartAgent()) {
                updateClick = TextComponent.fromLegacy(color("&6&lDownload the latest version now from "));
                updateClick.addExtra(polymart);
            } else if (isBuiltByBitAgent()) {
                updateClick = TextComponent.fromLegacy(color("&6&lDownload the latest version now from "));
                updateClick.addExtra(builtByBit);
            } else if (isSpigotMCAgent()) {
                updateClick = TextComponent.fromLegacy(color("&6&lDownload the latest version now from "));
                updateClick.addExtra(spigot);
            } else {
                updateClick = TextComponent.fromLegacy(color("&6&lGet and compile the latest source code now from "));
                updateClick.addExtra(github);
            }

            messageComponent.addExtra("\n");
            messageComponent.addExtra(changelogComponent);
            messageComponent.addExtra(updateClick);
        }

        if (player.hasPermission("closedblocks.update") || player.hasPermission("closedblocks.*")
                || player.isOp()) {
            player.spigot().sendMessage(this.messageComponent);
        }
    }

    @SuppressWarnings("ConstantValue")
    public boolean isPolymartAgent() {
        return !"%%__POLYMART__%%".equalsIgnoreCase(DOWNLOAD_AGENT_POLYMART);
    }

    @SuppressWarnings("ConstantValue")
    public boolean isBuiltByBitAgent() {
        return !"%%__VERSION__%%".equalsIgnoreCase(DOWNLOAD_AGENT_BUILTBYBIT);
    }

    @SuppressWarnings("ConstantValue")
    public boolean isSpigotMCAgent() {
        return !"%%__RESOURCE__%%".equalsIgnoreCase(DOWNLOAD_AGENT_SPIGOTMC);
    }

    private String color(final String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    private void check() {
        if (requiresUpdate)
            return;

        Gson gson = new GsonBuilder().setLenient().create();
        try {
            URL url = new URL(CHECK_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try (InputStream response = connection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(response, StandardCharsets.UTF_8)) {

                JsonObject data = gson.fromJson(isr, JsonObject.class);
                JsonObject stats = data.getAsJsonObject("stats");

                int updates = stats.get("updates").getAsInt();
                int maxPage = updates / 10;
                int remaining = updates % 10;
                if (remaining > 0)
                    maxPage += 1;

                maxPage = Math.max(1, maxPage);
                currentVersion = data.get("current_version").getAsString();

                if (!currentVersion.equalsIgnoreCase(this.plugin.getDescription().getVersion())) {
                    fetchChangelog(maxPage);
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check for updates on spigotmc", ex);
        }
    }

    private void fetchChangelog(final int page) {
        Gson gson = new GsonBuilder().setLenient().create();

        try {
            URL url = new URL(String.format(CHANGELOG_URL, page));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try (InputStream response = connection.getInputStream();
                 InputStreamReader isr = new InputStreamReader(response, StandardCharsets.UTF_8)) {

                JsonArray data = gson.fromJson(isr, JsonArray.class);
                for (JsonElement element : data) {
                    JsonObject updateInfo = element.getAsJsonObject();

                    this.updateId = updateInfo.get("id").getAsInt();
                    String updateVersion = updateInfo.get("resource_version").getAsString();
                    if (updateVersion.equalsIgnoreCase(this.currentVersion)) {
                        fetchUpdateInfo(updateInfo);
                        this.requiresUpdate = true;
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to check for updates on spigotmc", ex);
        }
    }

    private void fetchUpdateInfo(final JsonObject update) {
        String title = update.get("title").getAsString();
        String[] message = update.get("message").getAsString()
                .replaceAll("\\[.*?]", "")
                .split("\n");

        StringBuilder changelogBuilder = new StringBuilder("&3" + title);
        StringBuilder rawBuilder = new StringBuilder("&3" + title);

        changelogBuilder.append("&r\n");
        rawBuilder.append("&r\n");
        for (String content : message) {
            changelogBuilder.append("&b").append(truncateString(content)).append("&r\n");
            rawBuilder.append("&b").append(content).append("&r\n");
        }

        this.changelog = changelogBuilder.toString();
        this.rawChangelog = rawBuilder.toString();
    }

    private String truncateString(final String string) {
        StringBuilder truncated = new StringBuilder(string);

        int i = 90;
        while (i < truncated.length()) {
            truncated.insert(i, "&r\n&b");
            i += 91;
        }

        return truncated.toString();
    }
}