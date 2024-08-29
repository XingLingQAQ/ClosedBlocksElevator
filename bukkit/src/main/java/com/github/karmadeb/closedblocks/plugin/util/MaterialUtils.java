package com.github.karmadeb.closedblocks.plugin.util;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.util.NullableChain;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.closedblocks.plugin.integrations.shared.IntegrationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MaterialUtils {

    private static final ClosedBlocksPlugin plugin = JavaPlugin.getPlugin(ClosedBlocksPlugin.class);

    private MaterialUtils() {

    }

    @Nullable
    public static Material getMatchingMaterial(final String materialName, final Predicate<Material> filter, final Supplier<Material> def) {
        return NullableChain.of(() -> getByRegistry(materialName))
                .filter(filter)
                .or(() -> plugin.translateMaterial(materialName), filter)
                .or(() -> getByLegacyMatch(materialName), filter)
                .or(() -> getByMatch(materialName), filter)
                .or(() -> getByLegacyGet(materialName), filter)
                .or(() -> getByGet(materialName), filter)
                .or(() -> getByValueOf(materialName), filter)
                .orElseGet(def);
    }

    public static boolean isValidMaterial(final Material material, final ClosedBlock block) {
        if (block instanceof Elevator)
            return !IntegrationUtils.isIllegalElevatorType(material);

        return true;
    }

    private static Material getByRegistry(final String material) {
        try {
            Registry<Material> registry = Bukkit.getRegistry(Material.class);
            if (registry == null) return null;

            return registry.match(material.toLowerCase());
        } catch (Throwable ignored) {}

        return null;
    }

    private static Material getByLegacyMatch(final String material) {
        try {
            return Material.matchMaterial(material.toUpperCase(), true);
        } catch (Throwable ignored) {}

        return null;
    }

    private static Material getByMatch(final String material) {
        try {
            return Material.matchMaterial(material.toUpperCase(), false);
        } catch (Throwable ignored) {}

        return null;
    }

    private static Material getByLegacyGet(final String material) {
        try {
            return Material.getMaterial(material.toUpperCase(), true);
        } catch (Throwable ignored) {}

        return null;
    }

    private static Material getByGet(final String material) {
        try {
            return Material.getMaterial(material.toUpperCase(), false);
        } catch (Throwable ignored) {}

        return null;
    }

    private static Material getByValueOf(final String material) {
        try {
            return Material.valueOf(material.toUpperCase());
        } catch (Throwable ignored) {}

        return null;
    }
}
