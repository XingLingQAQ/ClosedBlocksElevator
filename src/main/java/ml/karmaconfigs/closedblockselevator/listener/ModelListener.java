package ml.karmaconfigs.closedblockselevator.listener;

import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.storage.custom.ModelConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ModelListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Client client = new Client(player);

        Config config = new Config();
        Messages messages = new Messages();
        int model = config.elevatorModel();
        if (model != -1) {
            ModelConfiguration m_Config = config.modelConfiguration();
            if (!StringUtils.isNullOrEmpty(m_Config.downloadURL())) {
                if (m_Config.prompt()) {
                    client.send(messages.requestTextures());
                }

                try {
                    player.setResourcePack(m_Config.downloadURL(), m_Config.hash());
                } catch (Throwable ex) {
                    player.setResourcePack(m_Config.downloadURL());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onResourceResult(PlayerResourcePackStatusEvent e) {
        Player player = e.getPlayer();
        Client client = new Client(player);

        Config config = new Config();
        Messages messages = new Messages();
        client.acceptTextures();

        switch (e.getStatus()) {
            case DECLINED:
            case FAILED_DOWNLOAD:
                int model = config.elevatorModel();
                if (model != -1) {
                    ModelConfiguration m_Config = config.modelConfiguration();
                    if (!StringUtils.isNullOrEmpty(m_Config.downloadURL())) {
                        client.unTexturize();

                        if (m_Config.force()) {
                            player.kickPlayer(StringUtils.toColor(messages.deniedTextures()));
                        }
                    }
                }
                break;
        }
    }
}
