package com.github.karmadeb.closedblocks.plugin.provider.file;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.configuration.Configuration;
import com.github.karmadeb.closedblocks.api.file.configuration.declaration.FileDeclaration;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import com.github.karmadeb.kyle.YamlContent;

public class ConfigurationFile extends PluginFile implements Configuration {

    public ConfigurationFile(final ClosedBlocksPlugin plugin) {
        super(plugin, "config");
    }

    /**
     * Get a value
     *
     * @param declaration the value declaration
     * @return the value
     */
    @Override
    public <T> T getValue(final FileDeclaration<T> declaration) {
        FileComponent component = declaration.getComponent();
        YamlContent content = contentMap.get(component);

        Object value = content.get(declaration.getPath(), declaration.getDefault());
        return declaration.casted(value);
    }
}
