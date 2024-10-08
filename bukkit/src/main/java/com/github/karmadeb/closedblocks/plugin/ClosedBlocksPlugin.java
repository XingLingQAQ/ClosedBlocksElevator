package com.github.karmadeb.closedblocks.plugin;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.event.plugin.ClosedPluginStateChangedEvent;
import com.github.karmadeb.closedblocks.api.file.messages.PluginMessages;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.BukkitIntegration;
import com.github.karmadeb.closedblocks.plugin.integrations.discordsrv.DiscordSRVIntegration;
import com.github.karmadeb.closedblocks.plugin.integrations.itemsadder.ItemsAdderIntegration;
import com.github.karmadeb.closedblocks.plugin.loader.BlockLoader;
import com.github.karmadeb.closedblocks.plugin.loader.BukkitLoader;
import com.github.karmadeb.closedblocks.plugin.util.ParticleUtils;
import com.github.karmadeb.closedblocks.plugin.util.inventory.ClosedBlockManager;
import com.github.karmadeb.closedblocks.plugin.util.version.VersionChecker;
import com.github.karmadeb.functional.helper.Colorize;
import com.github.karmadeb.functional.inventory.helper.PagedInventory;
import com.github.karmadeb.functional.inventory.helper.functional.PageItemMeta;
import com.github.karmadeb.functional.inventory.helper.page.InventoryPaginated;
import com.github.karmadeb.kson.element.JsonElement;
import com.github.karmadeb.kson.element.JsonObject;
import com.github.karmadeb.kson.parser.JsonParser;
import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@SuppressWarnings("FieldCanBeLocal")
public final class ClosedBlocksPlugin extends JavaPlugin {

    private final String LICENSED_TO_USER_ID = "%%__USER__%%";
    private final String LICENSED_TO_USERNAME = "%%__USERNAME__%%";

    private final BukkitLoader loader = new BukkitLoader(this);
    private final ClosedBlocksAPI api = new ClosedBlocksAPI(this);
    private final VersionChecker checker = new VersionChecker(this);
    private final Map<UUID, ClosedBlockManager> managingBlock = new ConcurrentHashMap<>();

    private ItemsAdderIntegration itemsAdderIntegration;
    private DiscordSRVIntegration discordSRVIntegration;

    @Override
    public void onEnable() {
        BlockLoader blockLoader = new BlockLoader(this, this.api);

        ClosedPluginStateChangedEvent event = new ClosedPluginStateChangedEvent(ClosedPluginStateChangedEvent.State.START);
        Bukkit.getPluginManager().callEvent(event);

        getLogger().info("Preparing to load stored blocks data. This may take a while...");
        blockLoader.load();
        getLogger().info("Loaded " + api.getBlockStorage().size() + " blocks");

        api.register();

        if (!NBT.preloadApi()) {
            getLogger().severe("Failed to initialize NBT support. ClosedBlocks depends on NBT to work, disabling...");
            getPluginLoader().disablePlugin(this);
            return;
        }

        Runnable task = () -> {
            BukkitIntegration integration = new BukkitIntegration(this);
            discordSRVIntegration = new DiscordSRVIntegration(this);

            api.addIntegration(integration);
            api.addIntegration(discordSRVIntegration);

            checker.start();
            sendThanksMessage();

            loader.registerEvents();
            loader.registerCommands();
            loader.setupParticleAPI();
            loader.createBlockVisualizer();

            api.getRecipeManager().loadRecipes();
        };

        itemsAdderIntegration = new ItemsAdderIntegration(this, task);
        api.addIntegration(itemsAdderIntegration);

        if (!itemsAdderIntegration.isSupported())
            task.run();
    }

    @SuppressWarnings("ConstantValue")
    private void sendThanksMessage() {
        if (LICENSED_TO_USER_ID.contains("__USER__") && LICENSED_TO_USERNAME.contains("__USERNAME__")) {
            getLogger().warning("Self compiled version of ClosedBlocks detected. You won't receive official plugin updates!");
        } else {
            String username = LICENSED_TO_USERNAME;
            if ("%%__USERNAME__%%".equalsIgnoreCase(LICENSED_TO_USERNAME)) {
                username = getSpigotUsername();
                if (username == null) {
                    getLogger().warning("Self compiled version of ClosedBlocks detected. You won't receive official plugin updates!");
                    return;
                }
            }

            String profileURL;
            if (this.checker.isPolymartAgent()) {
                profileURL = "https://polymart.org/user/" + LICENSED_TO_USER_ID;
            } else if (this.checker.isBuiltByBitAgent()) {
                profileURL = "https://builtbybit.com/members/" + LICENSED_TO_USER_ID;
            } else {
                profileURL = "https://www.spigotmc.org/members/" + LICENSED_TO_USER_ID;
            }

            Bukkit.getConsoleSender().sendMessage(
                    Colorize.colorize(
                            "&6This ClosedBlocks instance is licensed to&7 " + username + "&8 (&7" + profileURL + "&8)")
            );
        }
    }

    @Override
    public void onDisable() {
        loader.shutdown();

        checker.stop();
        api.shutdown();

        ClosedPluginStateChangedEvent event = new ClosedPluginStateChangedEvent(ClosedPluginStateChangedEvent.State.STOP);
        Bukkit.getPluginManager().callEvent(event);
    }

    public Path getDataPath() {
        return this.getDataFolder().toPath();
    }

    public VersionChecker getChecker() {
        return this.checker;
    }

    public ItemsAdderIntegration getItemsAdderIntegration() {
        return this.itemsAdderIntegration;
    }

    public DiscordSRVIntegration getDiscordSRVIntegration() {
        return this.discordSRVIntegration;
    }

    public ParticleUtils getParticleAPI() {
        return this.loader.getParticleUtils();
    }

    public void setManagingBlock(final Player player, final ClosedBlockManager manager) {
        if (manager == null) {
            this.managingBlock.remove(player.getUniqueId());
            return;
        }

        this.managingBlock.put(player.getUniqueId(), manager);
    }

    @SuppressWarnings("t")
    public void closeManagingBlock(final ClosedBlock block) {
        for (UUID uniqueId : this.managingBlock.keySet()) {
            Player player = Bukkit.getPlayer(uniqueId);
            if (player == null || !player.isOnline()) {
                this.managingBlock.remove(uniqueId);
                continue;
            }

            Inventory inventory = player.getOpenInventory().getTopInventory();

            ClosedBlockManager instance = this.managingBlock.get(uniqueId);
            if (instance.getBlock().equals(block)) {
                instance.finish();

                PluginMessages.BLOCK_MANAGE_REMOVED.send(player);

                PagedInventory<InventoryPaginated> paged = instance.getPage();
                InventoryPaginated paginated = paged.getPaginated();

                PageItemMeta meta = instance.getMeta();
                paged.removeItem(meta.getSlot());
                if (instance.isNotInventory(inventory)) {
                    paginated.open(player, paged.getPage() - 1);
                } else {
                    player.closeInventory();
                }

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    if (paged.isEmpty() && paged.getPage() > 1)
                        paginated.removePage(paged.getPage());
                }, 1);
                return;
            }
        }
    }

    public Material translateMaterial(final String name) {
        if (name.equalsIgnoreCase("powder")) {
            try {
                return Material.valueOf("SULPHUR");
            } catch (IllegalArgumentException ex) {
                return Material.valueOf("GUNPOWDER");
            }
        }

        return null;
    }

    private String getSpigotUsername() {
        try {
            URL url = new URL("https://api.spigotmc.org/simple/0.2/index.php?action=getAuthor&id=" + LICENSED_TO_USER_ID);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            try (InputStream response = connection.getInputStream()) {
                JsonParser parser = JsonParser.create(response);
                JsonElement element = parser.resolve();

                if (!element.isObject()) {
                    getLogger().severe("Failed to validate plugin license");
                    return null;
                }

                JsonObject object = element.getAsObject();
                return object.getAsString("username");
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Failed to validate plugin license", ex);
            return null;
        }
    }
}