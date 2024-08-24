package com.github.karmadeb.closedblocks.plugin.provider.file;

import com.github.karmadeb.closedblocks.api.file.FileComponent;
import com.github.karmadeb.closedblocks.api.file.messages.Messages;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageDeclaration;
import com.github.karmadeb.closedblocks.api.file.messages.declaration.MessageParameter;
import com.github.karmadeb.closedblocks.plugin.ClosedBlocksPlugin;
import es.karmadev.api.kyle.yaml.YamlContent;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MessagesFile extends PluginFile implements Messages {

    public MessagesFile(final ClosedBlocksPlugin plugin) {
        super(plugin, "messages");
    }

    /**
     * Get a message
     *
     * @param declaration the message declaration
     * @param parameters  the message parameters
     * @return the message
     */
    @Override
    public String getMessage(final MessageDeclaration declaration, final MessageParameter... parameters) {
        FileComponent component = declaration.getComponent();
        YamlContent content = contentMap.get(component);

        Object value = content.get(declaration.getPath());
        if (value == null)
            value = declaration.getDefault();

        if (value instanceof List) {
            List<String> lst = ((List<?>) value)
                    .stream().map(String::valueOf)
                    .collect(Collectors.toList());

            return parseList(lst, parameters);
        } else {
            String raw = String.valueOf(value);
            if (raw.contains("\n"))
                return parseList(Arrays.asList(raw.split("\n")), parameters);

            return parseRaw(raw, parameters);
        }
    }

    private String parseList(final Collection<String> message, final MessageParameter... parameters) {
        StringBuilder builder = new StringBuilder();
        for (String str : message)
            builder.append(parseRaw(str, parameters))
                    .append('\n');

        return builder.substring(0, builder.length() - 1);
    }

    private String parseRaw(final String message, final MessageParameter... parameters) {
        StringBuilder messageBuilder = new StringBuilder(message);

        if (parameters != null)
            for (MessageParameter parameter : parameters)
                parameter.mapTo(messageBuilder);

        return ChatColor.translateAlternateColorCodes('&',
                messageBuilder.toString());
    }
}
