package ml.karmaconfigs.closedblockselevator.command;

import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.storage.custom.ModelConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class TextureElevator implements CommandExecutor {

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
            Player issuer = (Player) sender;
            Client client = new Client(issuer);

            int model = config.elevatorModel();
            if (model != -1) {
                ModelConfiguration m_Config = config.modelConfiguration();
                if (!StringUtils.isNullOrEmpty(m_Config.downloadURL())) {
                    if (m_Config.prompt()) {
                        client.send(messages.requestTextures());
                    }

                    try {
                        issuer.setResourcePack(m_Config.downloadURL(), m_Config.hash());
                    } catch (Throwable ex) {
                        issuer.setResourcePack(m_Config.downloadURL());
                    }
                } else {
                    client.send("&cThis server does not use elevator textures");
                }
            } else {
                client.send("&cThis server does not use elevator textures");
            }
        } else {
            plugin.console().send("&cThis command is for players only!");
        }

        return false;
    }
}
