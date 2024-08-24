package com.github.karmadeb.closedblocks.api.file.configuration.elevator;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.configuration.declaration.FileDeclaration;

import java.util.Arrays;
import java.util.List;

/**
 * Represents elevator configuration keys
 */
public final class ElevatorConfig<T> implements FileDeclaration<T> {

    public static final FileDeclaration<String> DISGUISE = new ElevatorConfig<>("Disguise", "QUARTZ_BLOCK");
    public static final FileDeclaration<Integer> MAX_DISTANCE = new ElevatorConfig<>("MaxDistance", 64);
    public static final FileDeclaration<Boolean> ALLOW_BREAK = new ElevatorConfig<>("Griefing.AllowBreak", false);
    public static final FileDeclaration<Boolean> ALLOW_EXPLODE = new ElevatorConfig<>("Griefing.AllowExplode", false);
    public static final FileDeclaration<Boolean> ALLOW_BURN = new ElevatorConfig<>("Griefing.AllowBurn", false);
    public static final FileDeclaration<Boolean> ALLOW_GRIEF = new ElevatorConfig<>("Griefing.AllowGrief", false);
    public static final FileDeclaration<List<String>> DISGUISE_BLACKLIST = new ElevatorConfig<>("DisguiseBlacklist",
            Arrays.asList("BEDROCK", "DRAGON_EGG", "CHEST", "TRAP_CHEST", "ENDER_CHEST", "END_CRYSTAL")
    );

    private final String path;
    private final T value;

    private ElevatorConfig(final String path, final T value) {
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
        return FileComponent.ELEVATOR;
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
