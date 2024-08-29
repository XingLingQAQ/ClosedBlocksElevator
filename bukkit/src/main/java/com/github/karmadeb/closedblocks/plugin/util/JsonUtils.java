package com.github.karmadeb.closedblocks.plugin.util;

import com.github.karmadeb.kson.element.JsonArray;
import com.github.karmadeb.kson.element.JsonElement;
import com.github.karmadeb.kson.element.JsonObject;
import com.github.karmadeb.kson.element.JsonPrimitive;
import com.github.karmadeb.kson.parser.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class JsonUtils {

    private JsonUtils() {}

    public static JsonElement readFile(final Path file) {
        JsonParser parser = JsonParser.create(file);
        return parser.resolve();
    }

    public static @Nullable JsonObject getJsonObject(final JsonObject object, final String key) {
        if (!object.has(key)) return null;

        JsonElement minesInstance = object.get(key);
        if (!minesInstance.isObject())
            return null;

        return minesInstance.getAsObject();
    }

    public static String getString(final JsonObject object, final String key) {
        if (!object.has(key))
            return null;

        JsonElement instance = object.get(key);
        if (!instance.isPrimitive())
            return null;

        JsonPrimitive nat = instance.getAsPrimitive();
        if (!nat.isString()) return null;

        return nat.getAsString();
    }

    public static Boolean getBoolean(final JsonObject object, final String key) {
        if (!object.has(key))
            return null;

        JsonElement instance = object.get(key);
        if (!instance.isPrimitive())
            return null;

        JsonPrimitive nat = instance.getAsPrimitive();
        if (!nat.isBoolean()) return null;

        return nat.getAsBoolean();
    }

    public static Float getFloat(final JsonObject object, final String key) {
        if (!object.has(key))
            return null;

        JsonElement instance = object.get(key);
        if (!instance.isPrimitive())
            return null;

        JsonElement nat = instance.getAsPrimitive();
        if (!nat.isNumber()) return null;

        return nat.getAsFloat();
    }

    @SuppressWarnings("SameParameterValue")
    public static JsonArray getArray(final JsonObject object, final String key) {
        if (!object.has(key))
            return null;

        JsonElement instance = object.get(key);
        if (!instance.isArray())
            return null;

        return instance.getAsArray();
    }
}
