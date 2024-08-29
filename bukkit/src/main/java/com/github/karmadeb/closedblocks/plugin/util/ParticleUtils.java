package com.github.karmadeb.closedblocks.plugin.util;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.particle.type.ParticleTypeBlock;
import com.github.fierioziy.particlenativeapi.api.particle.type.ParticleTypeItemMotion;
import com.github.fierioziy.particlenativeapi.api.particle.type.ParticleTypeMotion;
import com.github.karmadeb.closedblocks.api.util.NullableChain;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public final class ParticleUtils {

    private final ClosedBlocksPlugin plugin;
    private final ParticleNativeAPI api;

    private final Sound FIRE = NullableChain.of(() -> tryGetSound("ITEM_FIRECHARGE_USE"))
            .or(() -> tryGetSound("FIRECHARGE_USE"))
            .or(() -> tryGetSound("ENTITY_GHAST_SHOOT"))
            .or(() -> tryGetSound("GHAST_SHOOT"))
            .or(() -> tryGetSound("GHAST_FIREBALL"))
            .orElse(Sound.values()[4]);

    public ParticleUtils(final ClosedBlocksPlugin plugin, final ParticleNativeAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    public void playDisguiseEffect(final World world, final Location location) {
        world.playSound(location, FIRE, 2f, 2f);
        if (api == null)
            return;

        ParticleTypeMotion motionParticle = api.LIST_1_8.FLAME;
        for (int i = 0; i < 10; i++)
            Bukkit.getScheduler().runTaskLater(plugin, () -> spawnBonfire(location, motionParticle), i);
    }

    public void playLineToDownEffect(final Location origin, final Collection<? extends Player> viewers) {
        if (api == null)
            return;

        ParticleTypeBlock particle = api.LIST_1_8.FALLING_DUST;
        spawnLineToBottom(origin, particle, viewers);
    }

    public void playLineToUpEffect(final Location origin, final Collection<? extends Player> viewers) {
        if (api == null)
            return;

        ParticleTypeBlock particle = api.LIST_1_8.FALLING_DUST;
        spawnLineToTop(origin, particle, viewers);
    }

    public void playMineEffect(final Location origin, final Collection<? extends Player> viewers, final boolean fire, final boolean broke) {
        if (api == null)
            return;

        ParticleTypeItemMotion particle = api.LIST_1_8.ITEM_CRACK;
        for (int i = 0; i < 4; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int j = 0; j < 360; j++) {
                    double angle = Math.toRadians(j);
                    double x = origin.getX() + (0.5 * Math.cos(angle));
                    double z = origin.getZ() + (0.5 * Math.sin(angle));
                    double y = origin.getY();

                    Location spawnLocation = new Location(origin.getWorld(), x, y, z);
                    particle.of((broke ? Material.GRAY_CONCRETE : Material.TNT)).packet(false, spawnLocation)
                            .sendTo(viewers);
                }

                if (fire)
                    api.LIST_1_8.LAVA.packet(false, origin).sendTo(viewers);
            }, 20 * i);
        }
    }

    public boolean isInvalid() {
        return this.api == null;
    }

    private void spawnBonfire(final Location location, final ParticleTypeMotion particle) {
        for (int i = 0; i < 360; i++) {
            double angle = Math.toRadians(i);
            double x = location.getX() + (0.95 * Math.cos(angle));
            double z = location.getZ() + (0.95 * Math.sin(angle));
            double y = location.getY();

            Location spawnLocation = new Location(location.getWorld(), x, y, z);
            Vector direction = new Vector(location.getX() - x, location.getY() - y + 1, location.getZ() - z).normalize();
            direction.setX(direction.getX() / 10);
            direction.setY(direction.getY() / 15);
            direction.setZ(direction.getZ() / 10);

            particle.packetMotion(false, spawnLocation, direction)
                    .sendInRadiusTo(Bukkit.getOnlinePlayers(), 32);
        }
    }

    private void spawnLineToBottom(final Location from, final ParticleTypeBlock particle, final Collection<? extends Player> viewers) {
        int interval = 0;
        for (double y = from.getY() + 1.15; y >= from.getY(); y -= 0.1) {
            Location destination = from.clone();
            destination.setY(y);

            Bukkit.getScheduler().runTaskLater(plugin, () -> particle.of(Material.REDSTONE_BLOCK)
                    .packet(false, destination)
                    .sendTo(viewers), ++interval);
        }
    }

    private void spawnLineToTop(final Location from, final ParticleTypeBlock particle, final Collection<? extends Player> viewers) {
        int interval = 0;
        for (double y = from.getY() - 1.15; y <= from.getY(); y += 0.1) {
            Location destination = from.clone();
            destination.setY(y);

            Bukkit.getScheduler().runTaskLater(plugin, () -> particle.of(Material.EMERALD_BLOCK)
                    .packet(false, destination)
                    .sendTo(viewers), ++interval);
        }
    }

    private Sound tryGetSound(final String name) {
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException ignored) {}
        return null;
    }
}
