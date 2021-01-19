package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.Key;

import java.util.Optional;

public interface DataHolderDelegate {

    CustomDataHolder getDataHolder();

    default <T> void set(Key<T> key, T value) {
        getDataHolder().set(key,value);
    }

    default void addFlag(String flag) {
        getDataHolder().addFlag(flag);
    }

    default void removeFlag(String flag) {
        getDataHolder().removeFlag(flag);
    }

    default void remove(Key<?> key) {
        getDataHolder().remove(key);
    }

    default boolean hasFlag(String flag) {
        return getDataHolder().hasFlag(flag);
    }

    default <T> Optional<T> get(Key<T> key) {
        return getDataHolder().get(key);
    }

    default <T> T getOrUse(Key<T> key, T def) {
        return getDataHolder().getOrUse(key, def);
    }

    default  <T> T getOrDefault(Key<T> key) {
        return getDataHolder().getOrDefault(key);
    }

}
