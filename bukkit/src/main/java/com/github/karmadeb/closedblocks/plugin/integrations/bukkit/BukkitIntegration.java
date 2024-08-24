package com.github.karmadeb.closedblocks.plugin.integrations.bukkit;

import com.github.karmadeb.closedblocks.api.integration.Integration;
import com.github.karmadeb.closedblocks.api.util.NullableChain;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events.BBlockInteractionListener;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events.BBlockPlaceRemoveListener;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events.BClosedBlockPlacedListener;
import com.github.karmadeb.closedblocks.plugin.integrations.bukkit.events.BClosedPluginListener;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import dev.lone.itemsadder.api.CustomBlock;
import es.karmadev.api.kson.*;
import es.karmadev.api.kson.io.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("SameParameterValue")
public class BukkitIntegration implements Integration {

    private static final String TYPE_ELEVATOR = "elevator";
    private final ClosedBlocksPlugin plugin;

    private final List<ShapedRecipe> registeredRecipes = new ArrayList<>();

    private BBlockPlaceRemoveListener blockPlaceOrRemoveListener;
    private BClosedPluginListener pluginStateListener;
    private BClosedBlockPlacedListener apiBlockPlacedListener;
    private BBlockInteractionListener interactionListener;

    public BukkitIntegration(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    public ClosedBlocksPlugin getPlugin() {
        return this.plugin;
    }

    /**
     * Get the integration name
     *
     * @return the name
     */
    @Override
    public String getName() {
        return "ClosedBlocks bukkit";
    }

    /**
     * Load the integration
     */
    @Override
    public void load() {
        if (failsToLoadRecipes(TYPE_ELEVATOR))
            return;

        blockPlaceOrRemoveListener = new BBlockPlaceRemoveListener(this);
        pluginStateListener = new BClosedPluginListener(this);
        apiBlockPlacedListener = new BClosedBlockPlacedListener(this);
        interactionListener = new BBlockInteractionListener(this);

        Bukkit.getPluginManager().registerEvents(blockPlaceOrRemoveListener, plugin);
        Bukkit.getPluginManager().registerEvents(pluginStateListener, plugin);
        Bukkit.getPluginManager().registerEvents(apiBlockPlacedListener, plugin);
        Bukkit.getPluginManager().registerEvents(interactionListener, plugin);
    }

    /**
     * Unload the integration
     */
    @Override
    public void unload() {
        try {
            registeredRecipes.forEach((recipe) -> Bukkit.removeRecipe(recipe.getKey()));
        } catch (Throwable ignored) {}

        HandlerList.unregisterAll(blockPlaceOrRemoveListener);
        HandlerList.unregisterAll(pluginStateListener);
        HandlerList.unregisterAll(apiBlockPlacedListener);
        HandlerList.unregisterAll(interactionListener);
    }

    /**
     * Get if the integration is supported
     *
     * @return if the integration is
     * supported by the plugin
     */
    @Override
    public boolean isSupported() {
        return true;
    }

    public boolean reloadRecipes() {
        try {
            registeredRecipes.forEach((recipe) -> Bukkit.removeRecipe(recipe.getKey()));
        } catch (Throwable ignored) {}

        return !failsToLoadRecipes(TYPE_ELEVATOR);
    }

    @Nullable
    public Material getMatchingMaterial(final String materialName, final Predicate<Material> filter, final Supplier<Material> def) {
        return NullableChain.of(() -> getByRegistry(materialName))
                .filter(filter)
                .or(() -> getByLegacyMatch(materialName), filter)
                .or(() -> getByMatch(materialName), filter)
                .or(() -> getByLegacyGet(materialName), filter)
                .or(() -> getByGet(materialName), filter)
                .or(() -> getByValueOf(materialName), filter)
                .orElseGet(def);
    }

    public boolean isValidMaterial(final Material material) {
        return material != null && material.isBlock();
    }

    public ItemStack createElevatorItem() {
        for (ShapedRecipe recipe : this.registeredRecipes) {
            ItemStack item = recipe.getResult();
            if (!NBT.get(item, (Function<ReadableItemNBT, Boolean>) (data) -> data.getString("closed_type").equals("elevator"))) continue;

            return item;
        }

        return null;
    }

    private Material getByRegistry(final String material) {
        try {
            Registry<Material> registry = Bukkit.getRegistry(Material.class);
            if (registry == null) return null;

            return registry.match(material.toLowerCase());
        } catch (Throwable ignored) {}

        return null;
    }

    private Material getByLegacyMatch(final String material) {
        try {
            return Material.matchMaterial(material.toUpperCase(), true);
        } catch (Throwable ignored) {}

        return null;
    }

    private Material getByMatch(final String material) {
        try {
            return Material.matchMaterial(material.toUpperCase(), false);
        } catch (Throwable ignored) {}

        return null;
    }

    private Material getByLegacyGet(final String material) {
        try {
            return Material.getMaterial(material.toUpperCase(), true);
        } catch (Throwable ignored) {}

        return null;
    }

    private Material getByGet(final String material) {
        try {
            return Material.getMaterial(material.toUpperCase(), false);
        } catch (Throwable ignored) {}

        return null;
    }

    private Material getByValueOf(final String material) {
        try {
            return Material.valueOf(material.toUpperCase());
        } catch (Throwable ignored) {}

        return null;
    }

    private boolean failsToLoadRecipes(final String type) {
        plugin.getLogger().info("Loading closed block " + type + " recipes...");
        Path typeRecipesDirectory = plugin.getDataPath().resolve("recipes").resolve(type);

        boolean exportRecipe = !Files.exists(typeRecipesDirectory) || getDirCount(typeRecipesDirectory) == 0;
        if (failsToCreateDirectory(typeRecipesDirectory))
            return true;

        if (exportRecipe && failsToExportResource(String.format("%s/default.recipe.json", type), typeRecipesDirectory.resolve("default.json")))
            return true;

        Collection<Path> files = listDirectory(typeRecipesDirectory);
        if (files == null)
            return true;

        int recipeCount = 0;
        for (Path file : files) {
            ShapedRecipe recipe = makeRecipe(type, file, recipeCount);
            if (recipe == null) continue;

            if (!Bukkit.addRecipe(recipe)) {
                plugin.getLogger().warning("Failed to register one recipe");
                continue;
            }

            registeredRecipes.add(recipe);
            recipeCount++;
        }

        plugin.getLogger().info("Registered " + recipeCount + " " + type + " recipes");
        return false;
    }

    private @Nullable ShapedRecipe makeRecipe(String type, Path file, final int recipeCount) {
        final String fileName = file.getFileName().toString();

        byte[] data = readFully(file);
        if (data == null) {
            plugin.getLogger().warning("Failed to parse file " + fileName + " as json");
            return null;
        }

        JsonInstance instance = dataToJson(data);
        if (instance == null)
            return null;

        if (!instance.isObjectType()) {
            plugin.getLogger().warning("Failed to parse file " + fileName + " as a json object");
            return null;
        }

        JsonObject recipeData = instance.asObject();
        ShapedRecipe recipe = readRecipe(recipeData, type, recipeCount);
        if (recipe == null) {
            plugin.getLogger().warning("Failed to create recipe from json file " + fileName);
            return null;
        }
        return recipe;
    }

    private ShapedRecipe readRecipe(final JsonObject data, final String type, final int recipeCount) {
        if (!data.hasChild("keys"))
            return null;

        JsonInstance keysInstance = data.getChild("keys");
        if (!keysInstance.isObjectType())
            return null;

        JsonObject keys = keysInstance.asObject();

        String[] shape = getRecipeShape(data);
        if (shape == null)
            return null;

        ItemStack resultItem = getItem(data);
        if (resultItem == null)
            return null;

        ShapedRecipe recipe = createNamelessRecipe(resultItem, type, recipeCount);
        if (recipe == null)
            return null;

        recipe.shape(shape);
        for (JsonInstance key : keys) {
            if (!key.isArrayType())
                continue;

            JsonArray keyArray = key.asArray();
            String keyChar = keyArray.getKey();

            List<ItemStack> choices = parseMaterialChoices(keyArray);

            RecipeChoice choice = new RecipeChoice.ExactChoice(choices);
            recipe.setIngredient(keyChar.charAt(0), choice);
        }

        return recipe;
    }

    private @NotNull List<ItemStack> parseMaterialChoices(JsonArray keyArray) {
        List<ItemStack> choices = new ArrayList<>();
        for (JsonInstance element : keyArray) {
            if (!element.isNativeType())
                continue;

            JsonNative nat = element.asNative();
            if (!nat.isString())
                continue;

            String rawMaterial = nat.asString();
            Material parsedMaterial = getMatchingMaterial(rawMaterial, (ignored) -> true, () -> null);
            ItemStack item;
            if (parsedMaterial == null) {
                if (plugin.getItemsAdderIntegration().has(rawMaterial)) {
                    item = CustomBlock.getInstance(rawMaterial).getItemStack();
                } else {
                    continue;
                }
            } else {
                item = new ItemStack(parsedMaterial);
            }

            choices.add(item);
        }

        return choices;
    }

    @SuppressWarnings("deprecation")
    private ShapedRecipe createNamelessRecipe(final ItemStack result, final String name, final int recipeCount) {
        try {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(String.format("%s_%d", name, recipeCount), plugin);
            if (key == null)
                return null;

            return new ShapedRecipe(key, result);
        } catch (Throwable ex) {
            return new ShapedRecipe(result);
        }
    }

    private ItemStack getItem(final JsonObject data) {
        if (!data.hasChild("resolves"))
            return null;

        JsonInstance resolvesInstance = data.getChild("resolves");
        if (!resolvesInstance.isObjectType())
            return null;

        JsonObject resolves = resolvesInstance.asObject();

        String rawType = getString(resolves, "type");
        if (plugin.getItemsAdderIntegration().has(rawType))
            return parseIAItem(rawType, resolves);

        Material type = getMatchingMaterial(rawType, (ignored) -> true, () -> null);
        if (type == null) {
            plugin.getLogger().warning("Failed to parse " + rawType + " as a recipe material because it doesn't exist or is not a block");
            return null;
        }

        ItemStack item = new ItemStack(type);

        if (failsToApplyStatsToItemStack(item, resolves)) return null;
        return item;
    }

    private ItemStack parseIAItem(final String name, final JsonObject resolves) {
        CustomBlock block = CustomBlock.getInstance(name);
        ItemStack stack = block.getItemStack();
        if (failsToApplyStatsToItemStack(stack, resolves)) return null;

        return stack;
    }

    private boolean failsToApplyStatsToItemStack(final ItemStack item, final JsonObject resolves) {
        String name = getString(resolves, "name");
        if (name == null)
            return true;

        List<String> lore = getArrayList(resolves, "description");
        NBT.modify(item, (modifier) -> {
            modifier.modifyMeta((nbt, meta) -> {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
                meta.addItemFlags(ItemFlag.values());
                meta.setLore(lore);
            });
            modifier.setString("closed_type", "elevator");
        });

        return false;
    }

    private String getString(final JsonObject data, final String key) {
        JsonInstance instance = data.getChild(key);
        if (!instance.isNativeType())
            return null;

        JsonNative nat = instance.asNative();
        if (!nat.isString())
            return null;

        return nat.getString();
    }

    private List<String> getArrayList(final JsonObject data, final String key) {
        JsonInstance instance = data.getChild(key);
        if (!instance.isArrayType())
            return null;

        JsonArray array = instance.asArray();
        List<String> items = new ArrayList<>();
        for (JsonInstance element : array) {
            if (!element.isNativeType())
                continue;

            JsonNative nat = element.asNative();
            if (!nat.isString())
                continue;

            items.add(ChatColor.translateAlternateColorCodes('&', nat.getString()));
        }

        return items;
    }

    private String[] getRecipeShape(final JsonObject data) {
        if (!data.hasChild("pattern"))
            return null;

        JsonInstance patternInstance = data.getChild("pattern");
        if (!patternInstance.isArrayType())
            return null;

        JsonArray array = patternInstance.asArray();
        String[] elements = new String[array.size()];

        int index = 0;
        for (JsonInstance element : array) {
            if (!element.isNativeType())
                return null;

            JsonNative nat = element.asNative();
            if (!nat.isString())
                return null;

            elements[index++] = nat.asString();
        }

        return elements;
    }

    private JsonInstance dataToJson(final byte[] data) {
        try {
            return JsonReader.parse(data);
        } catch (KsonException ex) {
            return null;
        }
    }

    private byte[] readFully(final Path file) {
        try {
           return Files.readAllBytes(file);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to read " + file, ex);
            return null;
        }
    }

    private Collection<Path> listDirectory(final Path directory) {
        if (Files.exists(directory) && !Files.isDirectory(directory))
            return null;

        try(Stream<Path> files = Files.list(directory)
                .filter((f) -> !Files.isDirectory(f))) {
            return files.collect(Collectors.toList());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get directory " + directory + " contents", ex);
            return null;
        }
    }

    private int getDirCount(final Path directory) {
        if (Files.exists(directory) && !Files.isDirectory(directory))
            return 0;

        try(Stream<Path> files = Files.list(directory)
                .filter((f) -> !Files.isDirectory(f))) {
            return (int) files.count();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get directory " + directory + " length", ex);
            return 0;
        }
    }

    private boolean failsToCreateDirectory(final Path directory) {
        if (Files.exists(directory) && Files.isDirectory(directory))
            return false;

        try {
            if (Files.exists(directory))
                Files.delete(directory);

            Files.createDirectories(directory);
            return false;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create directory " + directory, ex);
            return true;
        }
    }

    private boolean failsToExportResource(final String resource, final Path destination) {
        try(InputStream stream = plugin.getResource(resource)) {
            if (stream == null)
                throw new NullPointerException("Failed to export resource " + resource);

            Path directory = destination.getParent();
            if (failsToCreateDirectory(directory))
                return true;

            Files.copy(stream, destination);
            return false;
        } catch (Throwable ex) {
            plugin.getLogger().severe("Failed to export default elevator recipe");
            return true;
        }
    }
}
