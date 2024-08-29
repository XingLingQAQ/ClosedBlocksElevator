package com.github.karmadeb.closedblocks.plugin.loader;

import com.github.karmadeb.closedblocks.api.block.BlockType;
import com.github.karmadeb.closedblocks.api.item.RecipeManager;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.util.JsonUtils;
import com.github.karmadeb.closedblocks.plugin.util.MaterialUtils;
import com.github.karmadeb.functional.helper.Colorize;
import com.github.karmadeb.kson.element.JsonArray;
import com.github.karmadeb.kson.element.JsonElement;
import com.github.karmadeb.kson.element.JsonObject;
import com.github.karmadeb.kson.element.JsonPrimitive;
import de.tr7zw.changeme.nbtapi.NBT;
import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RecipeLoader implements RecipeManager {

    private final ClosedBlocksPlugin plugin;
    private final Map<BlockType<?>, Set<ShapedRecipe>> loadedRecipes = new HashMap<>();

    public RecipeLoader(final ClosedBlocksPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Tries to load all the recipes
     */
    @Override
    public void loadRecipes() {
        if (!this.loadedRecipes.isEmpty())
            return;

        for (BlockType<?> type: BlockType.values())
            this.loadRecipe(type);
    }

    /**
     * Reload all the recipes
     */
    @Override
    public void reloadRecipes() {
        this.unloadRecipes();
        for (BlockType<?> type: BlockType.values())
            this.loadRecipe(type);
    }

    /**
     * Unloads all the recipes
     */
    @Override
    public void unloadRecipes() {
        for (BlockType<?> type : BlockType.values()) {
            Set<ShapedRecipe> recipes = loadedRecipes.get(type);
            if (recipes == null) continue;

            Iterator<ShapedRecipe> iterator = recipes.iterator();
            while (iterator.hasNext()) {
                ShapedRecipe recipe = iterator.next();
                Bukkit.removeRecipe(recipe.getKey());
                iterator.remove();
            }
        }
    }

    /**
     * Get all the registered recipes
     *
     * @return the registered recipes
     */
    @Override
    public Collection<? extends Recipe> getRecipes() {
        return Collections.unmodifiableCollection(this.loadedRecipes.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
    }

    /**
     * Get all the registered recipes for
     * the specified block type
     *
     * @param type the block type
     * @return the block type recipes
     */
    @Override
    public Collection<? extends Recipe> getRecipes(final BlockType<?> type) {
        return Collections.unmodifiableCollection(this.loadedRecipes.getOrDefault(type, Collections.emptySet()));
    }

    private void loadRecipe(final BlockType<?> type) {
        Path typeRecipesDirectory = plugin.getDataPath().resolve("recipes").resolve(type.singular());

        boolean exportRecipe = !Files.exists(typeRecipesDirectory);
        if (exportRecipe && failsToExportResource(String.format("%s/default.recipe.json", type.singular()), typeRecipesDirectory.resolve("default.json")))
            throw new RuntimeException("Failed to export default recipe for " + type.singular());

        Collection<Path> files = listDirectory(typeRecipesDirectory);
        if (files == null)
            throw new RuntimeException("Failed to load recipes for " + type.singular());

        int recipeCount = 0;
        for (Path file : files) {
            ShapedRecipe recipe = makeRecipe(type, file, recipeCount);
            if (recipe == null) continue;

            if (!Bukkit.addRecipe(recipe)) {
                plugin.getLogger().warning("Failed to register one recipe");
                continue;
            }

            Set<ShapedRecipe> typeRecipes = loadedRecipes.computeIfAbsent(type, (s) -> new HashSet<>());
            typeRecipes.add(recipe);

            recipeCount++;
        }

        plugin.getLogger().info("Successfully loaded " + recipeCount + " recipe(s) for " + type.plural());
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
            plugin.getLogger().log(Level.SEVERE,"Failed to export default elevator recipe", ex);
            return true;
        }
    }

    private Collection<Path> listDirectory(final Path directory) {
        if (Files.exists(directory) && !Files.isDirectory(directory))
            return null;

        try(Stream<Path> files = Files.list(directory)
                .filter((f) -> !Files.isDirectory(f))
                .filter((f) -> f.getFileName().toString()
                        .endsWith(".json"))) {
            return files.collect(Collectors.toList());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get directory " + directory + " contents", ex);
            return null;
        }
    }

    private @Nullable ShapedRecipe makeRecipe(final BlockType<?> type, final Path file, final int recipeCount) {
        final String fileName = file.getFileName().toString();

        JsonElement instance = JsonUtils.readFile(file);
        if (!instance.isObject()) {
            plugin.getLogger().warning("Failed to parse file " + fileName + " as a json object");
            return null;
        }

        JsonObject recipeData = instance.getAsObject();
        ShapedRecipe recipe = readRecipe(recipeData, type, recipeCount);
        if (recipe == null) {
            plugin.getLogger().warning("Failed to create recipe from json file " + fileName);
            return null;
        }
        return recipe;
    }

    private ShapedRecipe readRecipe(final JsonObject data, final BlockType<?> type, final int recipeCount) {
        if (!data.has("keys"))
            return null;

        JsonElement keysInstance = data.get("keys");
        if (!keysInstance.isObject())
            return null;

        JsonObject keys = keysInstance.getAsObject();

        String[] shape = getRecipeShape(data);
        if (shape == null)
            return null;

        ItemStack resultItem = getItem(data, type);
        if (resultItem == null)
            return null;

        ShapedRecipe recipe = createRecipe(resultItem, type, recipeCount);
        if (recipe == null)
            return null;

        recipe.shape(shape);
        for (JsonElement key : keys) {
            if (!key.isArray())
                continue;

            JsonArray keyArray = key.getAsArray();
            String keyChar = keyArray.getKey();

            List<ItemStack> choices = parseMaterialChoices(keyArray);

            RecipeChoice choice = new RecipeChoice.ExactChoice(choices);
            recipe.setIngredient(keyChar.charAt(0), choice);
        }

        return recipe;
    }

    private String[] getRecipeShape(final JsonObject data) {
        if (!data.has("pattern"))
            return null;

        JsonElement patternInstance = data.get("pattern");
        if (!patternInstance.isArray())
            return null;

        JsonArray array = patternInstance.getAsArray();
        String[] elements = new String[array.size()];

        int index = 0;
        for (JsonElement element : array) {
            if (!element.isPrimitive())
                return null;

            JsonPrimitive nat = element.getAsPrimitive();
            if (!nat.isString())
                return null;

            elements[index++] = nat.getAsString();
        }

        return elements;
    }

    private ItemStack getItem(final JsonObject data, final BlockType<?> type) {
        if (!data.has("resolves"))
            return null;

        JsonElement resolvesInstance = data.get("resolves");
        if (!resolvesInstance.isObject())
            return null;

        JsonObject resolves = resolvesInstance.getAsObject();

        String rawType = JsonUtils.getString(resolves, "type");
        if (plugin.getItemsAdderIntegration().has(rawType))
            return parseIAItem(rawType, resolves, type);

        Material material = MaterialUtils.getMatchingMaterial(rawType, (ignored) -> true, () -> null);
        if (material == null) {
            plugin.getLogger().warning("Failed to parse " + rawType + " as a recipe material because it doesn't exist or is not a block");
            return null;
        }

        ItemStack item = new ItemStack(material);

        if (failsToApplyStatsToItemStack(item, resolves, type)) return null;
        return item;
    }

    private ItemStack parseIAItem(final String name, final JsonObject resolves, final BlockType<?> type) {
        CustomBlock block = CustomBlock.getInstance(name);
        ItemStack stack = block.getItemStack();
        if (failsToApplyStatsToItemStack(stack, resolves, type)) return null;

        return stack;
    }

    private boolean failsToApplyStatsToItemStack(final ItemStack item, final JsonObject resolves, final BlockType<?> type) {
        String name = JsonUtils.getString(resolves, "name");
        if (name == null)
            return true;

        JsonArray loreArray = JsonUtils.getArray(resolves, "description");
        if (loreArray == null)
            return true;

        List<String> lore = loreArray.getElements().stream()
                .map((element) -> Colorize.colorize(element.getAsString()))
                .collect(Collectors.toList());
        NBT.modify(item, (modifier) -> {
            modifier.modifyMeta((nbt, meta) -> {
                meta.setDisplayName(Colorize.colorize(name));
                meta.addItemFlags(ItemFlag.values());
                meta.setLore(lore);
            });
            modifier.setString("closed_type", type.singular());
        });

        return false;
    }

    private ShapedRecipe createRecipe(final ItemStack result, final BlockType<?> type, final int recipeCount) {
        NamespacedKey key = NamespacedKey.fromString(String.format("%s_%d", type.singular(), recipeCount), this.plugin);
        if (key == null)
            return null;

        return new ShapedRecipe(key, result);
    }

    private @NotNull List<ItemStack> parseMaterialChoices(final JsonArray keyArray) {
        List<ItemStack> choices = new ArrayList<>();
        for (JsonElement element : keyArray) {
            if (!element.isPrimitive())
                continue;

            JsonPrimitive nat = element.getAsPrimitive();
            if (!nat.isString())
                continue;

            String rawMaterial = nat.getAsString();
            Material parsedMaterial = MaterialUtils.getMatchingMaterial(rawMaterial, (ignored) -> true, () -> null);
            ItemStack item = null;
            if (parsedMaterial == null) {
                if (plugin.getItemsAdderIntegration().has(rawMaterial)) {
                    item = CustomBlock.getInstance(rawMaterial).getItemStack();
                }
            } else {
                item = new ItemStack(parsedMaterial);
            }
            if (item == null || item.getType().isAir())
                continue;

            choices.add(item);
        }

        return choices;
    }
}