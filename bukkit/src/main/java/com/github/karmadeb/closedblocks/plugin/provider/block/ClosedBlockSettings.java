package com.github.karmadeb.closedblocks.plugin.provider.block;

import com.github.karmadeb.closedblocks.api.block.BlockSettings;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClosedBlockSettings implements BlockSettings {

    private static final String EMPTY_STRING = "";

    private String name = EMPTY_STRING;
    private String disguise = EMPTY_STRING;
    private boolean enabled = true;
    private boolean particles = true;
    private boolean visible = false;
    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();

    public ClosedBlockSettings() {}

    public ClosedBlockSettings(final @NotNull String name, final @NotNull String disguise, final boolean enabled,
                               final boolean particles, final boolean visible, final @NotNull Collection<UUID> viewers) {
        this.name = name;
        this.disguise = disguise;
        this.enabled = enabled;
        this.particles = particles;
        this.visible = visible;
        this.viewers.addAll(viewers);
    }

    /**
     * Get the block name. Which helps
     * to identify a block in the general
     * block settings
     *
     * @return the block name
     */
    @Override
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Update the block name
     *
     * @param name the new block name
     */
    @Override
    public void setName(final @NotNull String name) {
        this.name = name;
    }

    /**
     * Get the block disguise material
     *
     * @return the block disguise
     */
    @Override
    public @NotNull String getDisguise() {
        return this.disguise;
    }

    /**
     * Update the block disguise
     *
     * @param disguise the new block disguise
     */
    @Override
    public void setDisguise(final @NotNull String disguise) {
        this.disguise = disguise;
    }

    /**
     * Get if the block is currently
     * enabled. When a block is disabled,
     * only the disguise will remain, all
     * other functionalities will be stopped
     *
     * @return if the block is enabled
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set if the block is enabled
     *
     * @param enabled the new block status
     */
    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Get if the block has particles
     * enabled
     *
     * @return if the particles are enabled
     * for the elevator
     */
    @Override
    public boolean hasParticles() {
        return this.particles;
    }

    /**
     * Set if the block displays
     * particles
     *
     * @param status the particle display
     *               status
     */
    @Override
    public void setParticles(final boolean status) {
        this.particles = status;
    }

    /**
     * Get if the block is visible
     *
     * @return if the block is visible
     * to everyone
     */
    @Override
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Get if the block is visible for
     * everyone. When the elevator is set to
     * be visible, nobody will see the "disguise"
     * block, instead they will see the original
     * block
     *
     * @param visible the block visibility status
     */
    @Override
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * Get the viewers of the
     * block
     *
     * @return a collection of the players
     * who can see the block
     */
    @Override
    public @NotNull Collection<OfflinePlayer> getViewers() {
        return Collections.unmodifiableCollection(this.viewers.stream().map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList()));
    }

    /**
     * Get if the block can be seen by
     * the specified player
     *
     * @param player the player
     * @return if the player can see the block
     */
    @Override
    public boolean canBeSeen(final @NotNull OfflinePlayer player) {
        return this.viewers.contains(player.getUniqueId());
    }

    /**
     * Add a viewer to the block
     *
     * @param player the player to add
     */
    @Override
    public void addViewer(final @NotNull OfflinePlayer player) {
        this.viewers.add(player.getUniqueId());
    }

    /**
     * Remove a viewer from the block
     *
     * @param player the player to remove
     */
    @Override
    public void removeViewer(final @NotNull OfflinePlayer player) {
        this.viewers.remove(player.getUniqueId());
    }
}
