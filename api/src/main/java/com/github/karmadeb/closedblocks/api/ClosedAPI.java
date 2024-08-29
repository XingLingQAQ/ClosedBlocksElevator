package com.github.karmadeb.closedblocks.api;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.file.configuration.Configuration;
import com.github.karmadeb.closedblocks.api.file.configuration.declaration.FileDeclaration;
import com.github.karmadeb.closedblocks.api.file.messages.Messages;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.api.item.ItemType;
import com.github.karmadeb.closedblocks.api.item.RecipeManager;
import com.github.karmadeb.closedblocks.api.storage.BlockStorage;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents the ClosedBlocks API
 */
public abstract class ClosedAPI {

    private static ClosedAPI instance;

    /**
     * Define the ClosedAPI instance
     * to the current
     */
    protected synchronized void setAsInstance() {
        if (instance != null)
            throw new IllegalStateException("Cannot define ClosedAPI because it has been already defined");

        instance = this;
    }

    /**
     * Get the plugin block storage
     *
     * @return the block storage
     */
    public abstract BlockStorage getBlockStorage();

    /**
     * Adds an integration to the API
     *
     * @param integration the integration to add
     */
    public abstract void addIntegration(final @NotNull Integration integration);

    /**
     * Removes an integration from the
     * API
     *
     * @param integration the integration to remove
     */
    public abstract void removeIntegration(final @NotNull Integration integration);

    /**
     * Get the recipe manager
     *
     * @return the recipe manager
     */
    public abstract RecipeManager getRecipeManager();

    /**
     * Get the plugin messages
     *
     * @return the plugin messages
     */
    public abstract Messages getMessages();

    /**
     * Get the plugin configuration
     *
     * @return the plugin configuration
     */
    public abstract Configuration getConfig();

    /**
     * Get the API instance
     *
     * @return the API instance
     */
    public static ClosedAPI getInstance() {
        return instance;
    }

    /**
     * Get a plugin message
     *
     * @param declaration the message declaration
     * @param parameters the message parameters
     * @return the message
     */
    public static String getMessage(final MessageDeclaration declaration, final MessageParameter... parameters) {
        if (instance == null) {
            StringBuilder def = new StringBuilder(declaration.getDefault());
            if (parameters != null)
                for (MessageParameter param : parameters)
                    param.mapTo(def);

            return ChatColor.translateAlternateColorCodes('&', def.toString());
        }

        return instance.getMessages().getMessage(declaration, parameters);
    }

    /**
     * Get a plugin configuration
     * value
     *
     * @param declaration the configuration declaration
     * @return the configuration value
     */
    public static <T> T getConfiguration(final FileDeclaration<T> declaration) {
        if (instance == null)
            return declaration.getDefault();

        return instance.getConfig().getValue(declaration);
    }

    /**
     * Create an item of the specified
     * block type
     *
     * @param type the block type
     * @return the block type item
     * @param <T> the block type
     */
    public static <T extends ClosedBlock> ItemStack createItem(final BlockType<T> type) {
        if (instance != null) {
            RecipeManager manager = instance.getRecipeManager();
            Collection<? extends Recipe> recipe = manager.getRecipes(type);
            if (recipe.isEmpty())
                return null;

            Recipe any = recipe.stream().findAny().orElse(null);
            return any.getResult();
        }

        throw new IllegalStateException("ClosedAPI has not been yet initialized!");
    }

    /**
     * Create an item of the specified
     * item type
     *
     * @param type the item type
     * @return the item type item
     */
    public static ItemStack createItem(final ItemType type) {
        if (instance != null) {
            RecipeManager manager = instance.getRecipeManager();
            Recipe any = manager.getRecipe(type);

            return any.getResult();
        }

        throw new IllegalStateException("ClosedAPI has not been yet initialized!");
    }
}