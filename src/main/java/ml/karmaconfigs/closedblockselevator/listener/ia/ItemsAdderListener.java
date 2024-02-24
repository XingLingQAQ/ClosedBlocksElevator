package ml.karmaconfigs.closedblockselevator.listener.ia;

import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import dev.lone.itemsadder.api.Events.CustomBlockInteractEvent;
import dev.lone.itemsadder.api.Events.CustomBlockPlaceEvent;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.Main;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.OwnerStorage;
import ml.karmaconfigs.closedblockselevator.storage.elevator.Elevator;
import ml.karmaconfigs.closedblockselevator.storage.ElevatorStorage;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.storage.custom.IAdder;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.LAST_LINE_MATCHER;
import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;
public class ItemsAdderListener implements Listener {

    private Sound enderman_sound = null;
    private Sound block_break = null;

    private final ConcurrentMap<UUID, Long> iterationMap = new ConcurrentHashMap<>();

    public ItemsAdderListener() {
        Sound[] sounds = Sound.values();

        for (Sound sound : sounds) {
            if (sound.name().endsWith("ENDERMAN_TELEPORT")) {
                enderman_sound = sound;
            }
            if (sound.name().endsWith("STONE_BREAK") || sound.name().endsWith("DIG_STONE")) {
                block_break = sound;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoad(ItemsAdderLoadDataEvent e) {
        if (e.getCause().equals(ItemsAdderLoadDataEvent.Cause.FIRST_LOAD)) {
            Config config = new Config();

            if (Config.usesItemAdder()) {
                plugin.console().send("Successfully registered recipe with ItemsAdder", Level.INFO);
            }

            ShapedRecipe shaped = config.getRecipe();
            plugin.getServer().addRecipe(shaped);
        } else {
            plugin.console().send("ItemsAdder got reloaded. Any error support from ClosedBlocksElevator since this moment won't be granted", Level.INFO);
            plugin.logger().scheduleLog(Level.WARNING, "ItemsAdder got reloaded!");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractBlock(CustomBlockInteractEvent e) {
        Messages messages = new Messages();
        Config config = new Config();

        Player player = e.getPlayer();
        ItemStack hand = e.getItem();
        Block block = e.getBlockClicked().getRelative(e.getBlockFace());
        Block clicked = e.getBlockClicked();

        Client client = new Client(player);
        if (ElevatorStorage.isElevator(clicked)) {
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK:
                    if (!player.isSneaking()) {
                        if (hand != null && IAdder.isCustomItem(hand)) {
                            boolean handled = false;

                            ItemMeta meta = hand.getItemMeta();
                            if (meta != null) {
                                if (meta.hasLore()) {
                                    List<String> lore = meta.getLore();
                                    if (lore != null && !lore.isEmpty()) {
                                        String last_line = StringUtils.stripColor(lore.get(lore.size() - 1));
                                        if (last_line.equals(LAST_LINE_MATCHER)) {
                                            handled = true;

                                            double offset = config.offset();
                                            block = block.getLocation().add(0d, offset, 0d).getBlock();

                                            if (ElevatorStorage.addElevator(block, hand)) {
                                                if (clicked.getType().isInteractable() && !player.isSneaking()) {
                                                    e.setCancelled(true); //Prevent the elevator from being placed
                                                    return;
                                                }

                                                OwnerStorage.assign(player, block);
                                                client.send(messages.elevatorPlaced());
                                            } else {
                                                client.send(messages.elevatorFailed());
                                                e.setCancelled(true);
                                            }
                                        }
                                    }
                                }
                            }

                            if (!handled && ElevatorStorage.isElevator(clicked)) {
                                if (!player.isSneaking()) {
                                    Elevator elevator = ElevatorStorage.loadElevator(clicked);
                                    if (elevator != null) {
                                        if (!elevator.isSameCamouflage(hand)) {
                                            if (config.canCamouflage(player, clicked)) {
                                                if (ElevatorStorage.iaHideElevator(elevator, clicked, IAdder.getItem(hand))) {
                                                    if (enderman_sound != null) {
                                                        player.playSound(player.getLocation(), enderman_sound, 2f, 0.5f);
                                                    }

                                                    client.send(messages.elevatorHidden());
                                                } else {
                                                    client.send(messages.elevatorHideError());
                                                }

                                                e.setCancelled(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (hand == null || hand.getType().equals(Material.AIR)) {
                            if (OwnerStorage.isOwner(player, clicked)) {
                                long lastIteration = iterationMap.getOrDefault(player.getUniqueId(), -1l);
                                long now = System.currentTimeMillis();

                                if (now >= lastIteration) {
                                    OwnerStorage.setVisibility(clicked, !OwnerStorage.getVisibility(clicked));

                                    if (OwnerStorage.getVisibility(clicked)) {
                                        client.send(messages.elevatorVisible());
                                    } else {
                                        client.send(messages.elevatorInvisible());
                                    }

                                    now += 1000;
                                    iterationMap.put(player.getUniqueId(), now);
                                }

                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                    break;
                case LEFT_CLICK_BLOCK:
                    if (player.isSneaking() && (hand == null || hand.getType().equals(Material.AIR))) {
                        if (config.canBreak(player, clicked)) {
                            if (ElevatorStorage.destroyElevator(clicked)) {
                                clicked.getDrops().clear(); //We must make it drop an actual elevator, and not whatever it is

                                if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                                    ItemStack drop;
                                    if (Config.usesItemAdder()) {
                                        drop = IAdder.getItem();
                                    } else {
                                        drop = new ItemStack(config.elevatorItem(), 1);
                                    }
                                    ItemMeta dropMeta = drop.getItemMeta();
                                    if (dropMeta != null) {
                                        dropMeta.setDisplayName(config.elevatorItemName());
                                        dropMeta.setLore(config.elevatorItemLore());
                                        drop.setItemMeta(dropMeta);
                                    }

                                    player.getWorld().dropItem(clicked.getLocation(), drop);
                                }

                                client.send(messages.elevatorDestroyed());
                                OwnerStorage.remove(clicked);
                                if (block_break != null) {
                                    player.playSound(player.getLocation(), block_break, 2f, 1f);
                                }

                                CustomBlock cb = CustomBlock.byAlreadyPlaced(clicked);
                                cb.playBreakEffect();
                                cb.remove();
                            } else {
                                client.send(messages.elevatorDestroyFail());
                            }

                            e.setCancelled(true);
                        }
                    }
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlaceBlock(CustomBlockPlaceEvent e) {
        Messages messages = new Messages();
        Config config = new Config();

        Player player = e.getPlayer();
        Block block = e.getBlock();
        Block where = e.getPlacedAgainst();
        ItemStack hand = e.getItemInHand();

        double offset = config.offset();
        block = block.getLocation().add(0d, offset, 0d).getBlock();

        if (ElevatorStorage.isElevator(where) && !player.isSneaking()) {
            e.setCancelled(true);
            return; //We cancel the event if the player is hiding an elevator
        }

        Client client = new Client(player);
        boolean handled = false;
        if (hand != null) {
            ItemMeta meta = hand.getItemMeta();
            if (meta != null) {
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null && !lore.isEmpty()) {
                        String last_line = StringUtils.stripColor(lore.get(lore.size() - 1));
                        if (last_line.equals(LAST_LINE_MATCHER)) {
                            handled = true;

                            if (ElevatorStorage.addElevator(block, hand)) {
                                OwnerStorage.assign(player, block);
                                client.send(messages.elevatorPlaced());
                            } else {
                                client.send(messages.elevatorFailed());
                                e.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDestroyBlock(CustomBlockBreakEvent e) {
        Player player = e.getPlayer();

        Block block = e.getBlock();
        Client client = new Client(player);

        int offset = IAdder.getOffset(block);
        block = block.getLocation().add(0d, offset, 0d).getBlock();

        Messages messages = new Messages();
        Config config = new Config();

        if (ElevatorStorage.isElevator(block)) {
            Elevator elevator = ElevatorStorage.loadElevator(block);
            assert elevator != null;

            if (config.canBreak(player, block)) {
                e.setCancelled(true);

                if (ElevatorStorage.destroyElevator(block)) {
                    block.getDrops().clear(); //We must make it drop an actual elevator, and not whatever it is

                    if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                        ItemStack drop;
                        if (Config.usesItemAdder()) {
                            drop = IAdder.getItem();
                        } else {
                            drop = new ItemStack(config.elevatorItem(), 1);
                        }
                        ItemMeta meta = drop.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(config.elevatorItemName());
                            meta.setLore(config.elevatorItemLore());
                            drop.setItemMeta(meta);
                        }

                        player.getWorld().dropItem(block.getLocation(), drop);
                    }

                    CustomBlock cb = CustomBlock.byAlreadyPlaced(block);
                    cb.playBreakEffect();
                    cb.remove();

                    client.send(messages.elevatorDestroyed());
                    OwnerStorage.remove(block);
                } else {
                    client.send(messages.elevatorDestroyFail());
                }
            }
        }
    }
}
