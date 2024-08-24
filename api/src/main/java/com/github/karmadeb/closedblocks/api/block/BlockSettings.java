package com.github.karmadeb.closedblocks.api.block;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a closed block settings
 */
@SuppressWarnings("unused")
public interface BlockSettings {

    /**
     * Get the block name. Which helps
     * to identify a block in the general
     * block settings
     *
     * @return the block name
     */
    @NotNull
    String getName();

    /**
     * Update the block name
     *
     * @param name the new block name
     */
    void setName(final @NotNull String name);

    /**
     * Get the block disguise material
     *
     * @return the block disguise
     */
    @NotNull
    String getDisguise();

    /**
     * Update the block disguise
     *
     * @param disguise the new block disguise
     */
    void setDisguise(final @NotNull String disguise);

    /**
     * Get if the block is currently
     * enabled. When a block is disabled,
     * only the disguise will remain, all
     * other functionalities will be stopped
     *
     * @return if the block is enabled
     */
    boolean isEnabled();

    /**
     * Set if the block is enabled
     *
     * @param enabled the new block status
     */
    void setEnabled(final boolean enabled);

    /**
     * Get if the block is visible
     *
     * @return if the block is visible
     * to everyone
     */
    boolean isVisible();

    /**
     * Get if the block is visible for
     * everyone. When the elevator is set to
     * be visible, everyone will see the
     * elevator particles, regardless of the
     * block disguise status. Otherwise, only
     * viewers will see the elevator particles
     *
     * @param visible the block visibility status
     */
    void setVisible(final boolean visible);

    /**
     * Get the viewers of the
     * block
     *
     * @return a collection of the players
     * who can see the block
     */
    @NotNull
    Collection<OfflinePlayer> getViewers();

    /**
     * Get if the block can be seen by
     * the specified player
     *
     * @param player the player
     * @return if the player can see the block
     */
    boolean canBeSeen(final @NotNull OfflinePlayer player);

    /**
     * Add a viewer to the block
     *
     * @param player the player to add
     */
    void addViewer(final @NotNull OfflinePlayer player);

    /**
     * Remove a viewer from the block
     *
     * @param player the player to remove
     */
    void removeViewer(final @NotNull OfflinePlayer player);
}
