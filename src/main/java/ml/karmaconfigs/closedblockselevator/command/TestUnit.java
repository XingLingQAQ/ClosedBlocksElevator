package ml.karmaconfigs.closedblockselevator.command;

import ml.karmaconfigs.api.bukkit.reflection.skull.SkinSkull;
import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.util.ParticleUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class TestUnit implements CommandExecutor {

    private static Location location1;
    private static Location location2;

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Messages messages = new Messages();
        Config config = new Config();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Client client = new Client(player);

            if (args.length == 0) {
                client.send("&cSpecify an argument (set1, set2, unset1, unset2, play)");
            } else {
                switch (args[0].toLowerCase()) {
                    case "set1":
                        location1 = player.getLocation();
                        client.send("&aDone position #1");
                        break;
                    case "set2":
                        location2 = player.getLocation();
                        client.send("&aDone position #2");
                        break;
                    case "unset1":
                        location1 = null;
                        client.send("&cUndone position #1");
                        break;
                    case "unset2":
                        location2 = null;
                        client.send("&cUndone position #2");
                        break;
                    case "play":
                        Location front = player.getLocation().clone().add(player.getLocation().getDirection().clone().multiply(2));

                        ParticleUtil.showArrowDirections(player, front, location1 != null, location2 != null);
                        break;
                    default:
                        client.send("&cUnknown");
                }
            }
        } else {
            plugin.console().send("&aNot for server");
        }

        return false;
    }
}