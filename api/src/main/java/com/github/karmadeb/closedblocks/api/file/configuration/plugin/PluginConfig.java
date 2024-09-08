package com.github.karmadeb.closedblocks.api.file.configuration.plugin;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.configuration.declaration.FileDeclaration;

/**
 * Represents plugin configuration keys
 */
public final class PluginConfig<T> implements FileDeclaration<T> {

    public static final FileDeclaration<Boolean> LOG_ELEVATOR_PLACE = new PluginConfig<>("DiscordSRV.BlockPlace.Elevator", true);
    public static final FileDeclaration<Boolean> LOG_MINE_PLACE = new PluginConfig<>("DiscordSRV.BlockPlace.Mine", true);
    public static final FileDeclaration<Boolean> LOG_ELEVATOR_DISGUISE = new PluginConfig<>("DiscordSRV.BlockDisguise.Elevator", true);
    public static final FileDeclaration<Boolean> LOG_MINE_TRIGGER_ONCE = new PluginConfig<>("DiscordSRV.MineTriggered.Enabled", true);
    public static final FileDeclaration<Boolean> LOG_MINE_TRIGGER_CHAIN = new PluginConfig<>("DiscordSRV.MineTriggered.Chained", true);
    public static final FileDeclaration<Boolean> LOG_BLOCK_GIVE = new PluginConfig<>("DiscordSRV.BlockGive", true);

    private final String path;
    private final T value;

    private PluginConfig(final String path, final T value) {
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
        return FileComponent.PLUGIN;
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
