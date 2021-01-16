package com.shinybunny.cmdapi;

import java.util.UUID;
import java.util.function.Supplier;

public class Key<T> {

    private UUID id;
    private Supplier<T> defaultValue;

    public Key(Supplier<T> defaultValue) {
        this.id = UUID.randomUUID();
        this.defaultValue = defaultValue;
    }

    public static <T> Key<T> create() {
        return new Key<>(()->null);
    }

    public static <T> Key<T> def(T defaultValue) {
        return new Key<>(()->defaultValue);
    }

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
