package com.github.karmadeb.closedblocks.plugin.provider.file;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import es.karmadev.api.kyle.yaml.YamlContent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public abstract class PluginFile {

    protected final Map<FileComponent, YamlContent> contentMap = new HashMap<>();

    protected PluginFile(final ClosedBlocksPlugin plugin, final String genericName) {
        this.setupContentMap(plugin, genericName);
    }

    /**
     * Reloads the messages
     */
    public void reload() {
        contentMap.values().forEach(YamlContent::reload);
    }

    private void setupContentMap(final @NotNull ClosedBlocksPlugin plugin, final @NotNull String name) {
        Path root = plugin.getDataPath();
        for (FileComponent component : FileComponent.values()) {
            switch (name.toLowerCase()) {
                case "config":
                    if (!component.isSupportsConfig()) continue;
                    break;
                case "messages":
                    if (!component.isSupportsMessages()) continue;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown component type: " + name);
            }

            String resourcePath = String.format("%s/%s.yml", component.getResourcePath(), name);
            Path componentPath = root.resolve(component.getTargetPath().toLowerCase())
                    .resolve(String.format("%s.yml", name));

            if (!Files.exists(componentPath)) {
                tryCreatePath(componentPath);
                exportToPath(plugin, resourcePath, componentPath);
            }

            try (InputStream stream = plugin.getResource(resourcePath)) {
                if (stream == null)
                    throw new NullPointerException("Failed to load " + resourcePath);

                YamlContent content = YamlContent.load(componentPath, stream);
                content.validate();

                contentMap.put(component, content);
                plugin.getLogger().info("Loaded " + component.name().toLowerCase() + " " + name +  " component(s)");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void tryCreatePath(final Path file) {
        Path parent = file.getParent();
        try {
            if (!Files.exists(parent))
                Files.createDirectories(parent);

            Files.createFile(file);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void exportToPath(final ClosedBlocksPlugin plugin, final String resource, final Path file) {
        try (InputStream stream = plugin.getResource(resource)) {
            if (stream == null)
                throw new NullPointerException("Failed to export " + resource);

            Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
