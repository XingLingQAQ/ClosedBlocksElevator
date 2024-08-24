package com.github.karmadeb.closedblocks.api.file;

/**
 * Represents a file component. The component
 * defines the closed block environment, for example,
 * "elevators", or "backpacks"
 */
public enum FileComponent {
    PLUGIN("plugin", "", false, true),
    ELEVATOR("elevator", true, true);

    private final String resourcePath;
    private final String targetPath;
    private final boolean supportsConfig;
    private final boolean supportsMessages;

    FileComponent(final String resourcePath, final String targetPath,
                  final boolean config, final boolean messages) {
        this.resourcePath = resourcePath;
        this.targetPath = targetPath;
        this.supportsConfig = config;
        this.supportsMessages = messages;
    }

    FileComponent(final String resourcePath, final boolean supportsConfig, final boolean supportsMessages) {
        this(resourcePath, resourcePath,
                supportsConfig, supportsMessages);
    }

    /**
     * Get the component resource
     * path
     *
     * @return the resource path of the
     * component
     */
    public String getResourcePath() {
        return this.resourcePath;
    }

    /**
     * Get the component exported
     * resource path
     *
     * @return the target path
     */
    public String getTargetPath() {
        return this.targetPath;
    }

    /**
     * Get if the component supports configuration
     * files
     *
     * @return if the component supports config
     */
    public boolean isSupportsConfig() {
        return this.supportsConfig;
    }

    /**
     * Get if the component supports messages
     * files
     *
     * @return if the component supports messages
     */
    public boolean isSupportsMessages() {
        return this.supportsMessages;
    }
}
