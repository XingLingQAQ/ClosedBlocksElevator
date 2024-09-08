package com.github.karmadeb.closedblocks.api.file.configuration.mine;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.configuration.declaration.FileDeclaration;

/**
 * Represents elevator configuration keys
 */
public final class MineConfig<T> implements FileDeclaration<T> {

    public static final FileDeclaration<Boolean> CRAFTING = new MineConfig<>("Crafting", true);
    public static final FileDeclaration<Boolean> CRAFTING_DIFFUSER = new MineConfig<>("CraftingDiffuser", true);
    public static final FileDeclaration<String> DISGUISE = new MineConfig<>("Disguise", "AIR");
    public static final FileDeclaration<Number> POWER = new MineConfig<>("DefaultPower", 2f);
    public static final FileDeclaration<Number> MAX_POWER = new MineConfig<>("MaxPower", 16f);
    public static final FileDeclaration<Boolean> FIRE = new MineConfig<>("FireByDefault", false);
    public static final FileDeclaration<Boolean> OWNER_TRIGGER = new MineConfig<>("OwnerTriggers", true);
    public static final FileDeclaration<Boolean> CHAIN_EXPLOSION = new MineConfig<>("Griefing.ChainedExplosion", true);
    public static final FileDeclaration<Boolean> IGNITE_EXPLODE = new MineConfig<>("Griefing.IgniteExplosion", true);
    public static final FileDeclaration<Boolean> PICKUP_EXPLODE = new MineConfig<>("Griefing.PickupExplode", true);
    public static final FileDeclaration<Number> DIFFUSER_USAGES = new MineConfig<>("DiffuserMaxUsages", 255);

    private final String path;
    private final T value;

    private MineConfig(final String path, final T value) {
        this.path = path;
        this.value = value;
    }

    /**
     * Get the file component
     *
     * @return the component
     */
    @Override
    public FileComponent getComponent() {
        return FileComponent.MINE;
    }

    /**
     * Get the value path
     *
     * @return the path of the value
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * Get the default value
     *
     * @return the default value
     */
    @Override
    public T getDefault() {
        return this.value;
    }

    /**
     * Get a casted value
     *
     * @param value the value
     * @return the cast value
     */
    @Override
    @SuppressWarnings("unchecked")
    public T casted(final Object value) {
        return (T) value;
    }
}
