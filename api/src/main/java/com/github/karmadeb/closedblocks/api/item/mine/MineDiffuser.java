package com.github.karmadeb.closedblocks.api.item.mine;

import com.github.karmadeb.closedblocks.api.file.configuration.mine.MineConfig;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Objects;

/**
 * Represents a diffuser for a mine
 */
@SuppressWarnings("unused")
public final class MineDiffuser {

    public static final String USAGES_KEY = "diffuser_usages";
    public static final String MAX_USAGES_KEY = "diffuser_max_usages";

    private final ItemStack item;
    private int usages;

    /**
     * Create a mine diffuser from the
     * specified item
     * @param item the item
     */
    public MineDiffuser(final ItemStack item) {
        this.item = Objects.requireNonNull(item);
        this.usages = MineConfig.DIFFUSER_USAGES.get().intValue();
        this.load();
    }

    /**
     * Get the diffuser usages
     *
     * @return the amount of usages
     */
    public int getUsages() {
        return this.usages;
    }

    /**
     * Set the amount of usages remaining
     * for the diffuser
     *
     * @param usages the amount of usages
     */
    public void setUsages(final int usages) {
        this.usages = Math.max(0, Math.min(MineConfig.DIFFUSER_USAGES.get().intValue(), usages));
    }

    /**
     * Use the diffuser
     */
    public void use() {
        int currentUsages = this.getUsages();
        this.setUsages(currentUsages - 1);
    }

    /**
     * Load the item data
     */
    public void load() {
        NBT.modify(this.item, (nbt) -> {
            if (nbt.hasTag(MAX_USAGES_KEY, NBTType.NBTTagInt)) {
                Integer value = nbt.getInteger(MAX_USAGES_KEY);
                assert value != null;

                if (value != MineConfig.DIFFUSER_USAGES.get().intValue())
                    nbt.setInteger(MAX_USAGES_KEY, MineConfig.DIFFUSER_USAGES.get().intValue());
            }

            if (nbt.hasTag(USAGES_KEY, NBTType.NBTTagInt)) {
                Integer value = nbt.getInteger(USAGES_KEY);
                assert value != null;

                this.usages = value;
            }
        });
    }

    /**
     * Save the item data
     */
    public void save() {
        NBT.modify(this.item, (nbt) -> {
            nbt.setString("closed_type", "mine_diffuser");
            nbt.setInteger(MAX_USAGES_KEY, MineConfig.DIFFUSER_USAGES.get().intValue());
            nbt.setInteger(USAGES_KEY, this.usages);

            nbt.modifyMeta((nb, meta) -> {
                if (meta instanceof Damageable) {
                    Damageable damageable = (Damageable) meta;
                    damageable.setMaxDamage(MineConfig.DIFFUSER_USAGES.get().intValue());
                    damageable.setDamage(MineConfig.DIFFUSER_USAGES.get().intValue() - this.usages);
                }

                meta.setDisplayName(nb.getString("original_name").replace("{usages}",
                        String.valueOf(this.usages)));
            });
        });
    }

    /**
     * Get the mine diffuser item
     *
     * @return the item
     */
    public ItemStack getItem() {
        return this.item;
    }
}
