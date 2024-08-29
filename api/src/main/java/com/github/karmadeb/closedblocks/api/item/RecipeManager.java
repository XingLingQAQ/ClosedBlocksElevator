package com.github.karmadeb.closedblocks.api.item;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import org.bukkit.inventory.Recipe;

import java.util.Collection;

/**
 * Represents the plugin recipe
 * manager
 */
@SuppressWarnings("unused")
public interface RecipeManager {

    /**
     * Tries to load all the recipes
     */
    void loadRecipes();

    /**
     * Reload all the recipes
     */
    void reloadRecipes();

    /**
     * Unloads all the recipes
     */
    void unloadRecipes();

    /**
     * Get all the registered recipes
     *
     * @return the registered recipes
     */
    Collection<? extends Recipe> getRecipes();

    /**
     * Get all the registered recipes for
     * the specified block type
     *
     * @param type the block type
     * @return the block type recipes
     */
    Collection<? extends Recipe> getRecipes(final BlockType<?> type);
}
