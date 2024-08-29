package com.github.karmadeb.closedblocks.plugin.util;

import org.bukkit.Sound;

public final class SoundUtils {

    private SoundUtils() {

    }

    public static Sound tryGetSound(final String name) {
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException ignored) {}
        return null;
    }
}
