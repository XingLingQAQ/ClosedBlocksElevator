package com.github.karmadeb.closedblocks.api.file.messages.declaration;

import com.github.karmadeb.closedblocks.api.block.ClosedBlock;
import com.github.karmadeb.closedblocks.api.block.type.Elevator;
import com.github.karmadeb.closedblocks.api.block.type.Mine;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a simple message parameter
 */
@SuppressWarnings("unused")
public final class MessageParameter {

    private final String placeholder;
    private final Object value;

    private MessageParameter(final String placeholder, final Object value) {
        this.placeholder = placeholder;
        this.value = value;
    }

    /**
     * Map the parameter into the specified
     * message builder
     *
     * @param message the message builder to map
     *                to
     */
    public void mapTo(final StringBuilder message) {
        String needle = String.format("{%s}", this.placeholder);
        int index = message.indexOf(needle);

        while (index != -1) {
            message.replace(index, index + needle.length(), String.valueOf(value));
            index = message.indexOf(needle);
        }
    }

    /**
     * Create a message parameter for {player}
     * placeholder
     *
     * @param value the placeholder value
     * @return the message parameter
     */
    public static MessageParameter player(final @NotNull String value) {
        return create("player", value);
    }

    /**
     * Create a message parameter for {player}
     * placeholder
     *
     * @param value the placeholder value
     * @return the message parameter
     */
    public static MessageParameter player(final @NotNull OfflinePlayer value) {
        return create("player", value.getName());
    }

    /**
     * Create a message parameter for {floor}
     * placeholder
     *
     * @param floor the placeholder value
     * @return the message parameter
     */
    public static MessageParameter floor(final int floor) {
        return create("floor", Math.max(0, floor));
    }

    /**
     * Create a message parameter for {floor}
     * placeholder
     *
     * @param elevator the placeholder value
     * @return the message parameter
     */
    public static MessageParameter floor(final Elevator elevator) {
        return create("floor", Math.max(0, elevator.getFloor()));
    }

    /**
     * Create a message parameter for {floors}
     * placeholder
     *
     * @param floors the placeholder value
     * @return the message parameter
     */
    public static MessageParameter floors(final int floors) {
        return create("floors", Math.max(0, floors));
    }

    /**
     * Create a message parameter for {floor}
     * placeholder
     *
     * @param elevator the placeholder value
     * @return the message parameter
     */
    public static MessageParameter floors(final Elevator elevator) {
        return create("floors", Math.max(0, elevator.getFloors() - 1));
    }

    /**
     * Create a message parameter for {blocks}
     * placeholder
     *
     * @param amount the placeholder value
     * @return the message parameter
     */
    public static MessageParameter blocks(final int amount) {
        return create("blocks", amount);
    }

    /**
     * Create a message parameter for {permission}
     * placeholder
     *
     * @param permission the placeholder value
     * @return the message parameter
     */
    public static MessageParameter permission(final String permission) {
        return create("permission", permission);
    }

    /**
     * Create a message parameter for {floor}
     * placeholder
     *
     * @param permission the placeholder value
     * @return the message parameter
     */
    public static MessageParameter permission(final Permission permission) {
        return create("permission", permission.getName());
    }

    /**
     * Create a message parameter for {actions}
     * placeholder
     *
     * @param actions the placeholder value
     * @return the message parameter
     */
    public static MessageParameter actions(final String actions) {
        return create("actions", actions);
    }

    /**
     * Create a message parameter for {floor}
     * placeholder
     *
     * @param actions the placeholder value
     * @return the message parameter
     */
    public static MessageParameter actions(final Collection<String> actions) {
        return create("actions", String.join(", ", actions));
    }

    /**
     * Create a message parameter for {floor}
     * placeholder
     *
     * @param actions the placeholder value
     * @return the message parameter
     */
    public static MessageParameter actions(final String[] actions) {
        return create("actions", String.join(", ", actions));
    }

    /**
     * Create a message parameter for {label}
     * placeholder
     *
     * @param label the placeholder value
     * @return the message parameter
     */
    public static MessageParameter label(final String label) {
        return create("label", label);
    }

    /**
     * Create a message parameter for {action}
     * placeholder
     *
     * @param action the placeholder value
     * @return the message parameter
     */
    public static MessageParameter action(final String action) {
        return create("action", action);
    }

    /**
     * Create a message parameter for {slots}
     * placeholder
     *
     * @param slots the placeholder value
     * @return the message parameter
     */
    public static MessageParameter slots(final int slots) {
        return create("slots", slots);
    }

    /**
     * Create a message parameter for {amount}
     * placeholder
     *
     * @param amount the placeholder value
     * @return the message parameter
     */
    public static MessageParameter amount(final int amount) {
        return create("amount", amount);
    }

    /**
     * Create a message parameter for {type}
     * placeholder
     *
     * @param type the placeholder value
     * @return the message parameter
     */
    public static MessageParameter type(final ClosedBlock type) {
        if (type instanceof Elevator)
            return create("type", "elevator");
        if (type instanceof Mine)
            return create("type", "mine");

        return create("type", type.getClass().getSimpleName().toLowerCase());
    }

    /**
     * Create a message parameter for {type}
     * placeholder
     *
     * @param type the placeholder value
     * @return the message parameter
     */
    public static MessageParameter type(final String type) {
        return create("type", type);
    }

    /**
     * Create a message parameter for {name}
     * placeholder
     *
     * @param name the placeholder value
     * @return the message parameter
     */
    public static MessageParameter name(final String name) {
        return create("name", name);
    }

    /**
     * Create a message parameter for {world}
     * placeholder
     *
     * @param world the placeholder value
     * @return the message parameter
     */
    public static MessageParameter world(final World world) {
        return create("world", world.getName());
    }

    /**
     * Create a message parameter for {world}
     * placeholder
     *
     * @param world the placeholder value
     * @return the message parameter
     */
    public static MessageParameter world(final String world) {
        return create("world", world);
    }

    /**
     * Create a message parameter for {x}
     * placeholder
     *
     * @param x the placeholder value
     * @return the message parameter
     */
    public static MessageParameter x(final int x) {
        return create("x", x);
    }

    /**
     * Create a message parameter for {y}
     * placeholder
     *
     * @param y the placeholder value
     * @return the message parameter
     */
    public static MessageParameter y(final int y) {
        return create("y", y);
    }

    /**
     * Create a message parameter for {z}
     * placeholder
     *
     * @param z the placeholder value
     * @return the message parameter
     */
    public static MessageParameter z(final int z) {
        return create("z", z);
    }

    /**
     * Create a message parameter for {disguise}
     * placeholder
     *
     * @param disguise the placeholder value
     * @return the message parameter
     */
    public static MessageParameter disguise(final String disguise) {
        return create("disguise", disguise);
    }

    /**
     * Create a message parameter for {power}
     * placeholder
     *
     * @param mine the placeholder value
     * @return the message parameter
     */
    public static MessageParameter power(final Mine mine) {
        return create("power", mine.getPower());
    }

    /**
     * Create a message parameter for {power}
     * placeholder
     *
     * @param power the placeholder value
     * @return the message parameter
     */
    public static MessageParameter power(final float power) {
        return create("power", power);
    }

    /**
     * Create a new message parameter
     *
     * @param key the parameter key
     * @param value the parameter value
     * @return the message parameter
     */
    public static MessageParameter create(final @NotNull String key, final Object value) {
        return new MessageParameter(key, value);
    }
}
