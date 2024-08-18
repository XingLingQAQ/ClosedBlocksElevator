package com.github.karmadeb.closedblocks.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Nullable chain represents an object
 * which has many ways of being retrieved,
 * that a method chain is used until it founds
 * a non-null value
 * @param <T> the object type
 */
@FunctionalInterface
public interface NullableChain<T> extends Supplier<T> {

    /**
     * Create a nullable chain
     *
     * @param object the object supplier
     * @return the nullable chain
     * @param <T> the object type
     */
    static <T> NullableChain<T> of(final @NotNull Supplier<T> object) {
        return object::get;
    }

    /**
     * Create a nullable chain
     *
     * @param object the object supplier
     * @return the nullable chain
     * @param <T> the object type
     */
    static <T> NullableChain<T> of(final @Nullable T object) {
        return () -> object;
    }

    /**
     * Get the object
     *
     * @return the object
     */
    @Nullable
    T get();

    /**
     * Gets the object or a safe default
     * value instead
     *
     * @param safeValue the safe value
     * @return the object
     */
    @NotNull
    default T orElse(final @NotNull T safeValue) {
        T current = get();
        if (current == null)
            return safeValue;

        return current;
    }

    /**
     * Gets the object or provide a
     * safe one
     *
     * @param supplier the object provider
     * @return the object
     */
    default T orElseGet(final Supplier<T> supplier) {
        return orElse(supplier.get());
    }

    /**
     * Creates a chain to the current nullable
     * object
     *
     * @param supplier the chain provider
     * @return the nullable chain
     */
    @NotNull
    default NullableChain<T> or(final NullableChain<T> supplier) {
        return () -> get() == null ? supplier.get() : get();
    }

    /**
     * Creates a chain to the current nullable
     * object
     *
     * @param supplier the chain provider
     * @return the nullable chain
     */
    @NotNull
    default NullableChain<T> or(final Function<T, T> supplier) {
        return () -> supplier.apply(get());
    }

    /**
     * Creates a chain to the current nullable
     * object
     *
     * @param supplier the chain provider
     * @return the nullable chain
     */
    @NotNull
    default NullableChain<T> or(final NullableChain<T> supplier, final Predicate<T> filter) {
        return or(supplier).filter(filter);
    }

    /**
     * Creates a chain to the current nullable
     * object
     *
     * @param supplier the chain provider
     * @return the nullable chain
     */
    @NotNull
    default NullableChain<T> or(final Function<T, T> supplier, final Predicate<T> filter) {
        return or(supplier).filter(filter);
    }

    /**
     * Filter the chain result with
     * the predicate
     *
     * @param predicate the predicate
     * @return the filtered nullable chain
     */
    default NullableChain<T> filter(final Predicate<T> predicate) {
        return () -> {
            T element = get();
            return predicate.test(element) ? element : null;
        };
    }
}
