package com.shinybunny.hurricane.util;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * A key is used to set and access custom data inside {@link CustomDataHolder}s. A key has a Unique Id ({@link UUID}), and an optional default value.
 * A default value can be constant or a factory (<code>Supplier</code>).
 * Each time this key is accessed in a CustomDataHolder, if it is not associated with a value yet, the default value supplier is called and the returned value is used.
 * @param <T>
 */
public class Key<T> {

    private UUID id;
    private Supplier<T> defaultValue;

    private Key(Supplier<T> defaultValue) {
        this.id = UUID.randomUUID();
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a simple Key with a <code>null</code> default value.
     * @param <T> The type of value this key should be associated with
     */
    public static <T> Key<T> create() {
        return new Key<>(()->null);
    }

    /**
     * Creates a Key with the specified default value.
     * @param defaultValue The default value to use when no value is present yet.
     */
    public static <T> Key<T> def(T defaultValue) {
        return new Key<>(()->defaultValue);
    }

    /**
     * Creates a Key with the provided default value supplier. Each time this key is queried but is not set yet in the holder, this supplier is called.
     * @param defaultSupplier The default value supplier
     */
    public static <T> Key<T> def(Supplier<T> defaultSupplier) {
        return new Key<>(defaultSupplier);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Key && ((Key) obj).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public T getDefault() {
        return defaultValue.get();
    }
}
