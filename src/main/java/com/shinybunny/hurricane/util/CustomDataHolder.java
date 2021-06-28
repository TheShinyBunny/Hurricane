package com.shinybunny.hurricane.util;

import java.util.*;

public class CustomDataHolder {

    private final Map<Key<?>,Object> customData = new HashMap<>();
    private final Set<String> flags = new HashSet<>();

    /**
     * Sets a custom value in this object, with <code>key</code> as the {@link Key} of the <code>value</code>
     */
    public <T> void set(Key<T> key, T value) {
        this.customData.put(key,value);
    }

    /**
     * Adds a flag to this object. A flag can simply exist or not exist within a CustomDataHolder.
     * @param flag The flag name (should be unique)
     */
    public void addFlag(String flag) {
        flags.add(flag);
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    /**
     * Clears a key from its associated value in this object.
     * @param key The key to remove its value
     */
    public void remove(Key<?> key) {
        customData.remove(key);
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    /**
     * Gets the value associated with <code>key</code>.
     * If no value is present, will use the default value of the Key, or an empty optional if it returned null.
     * @param key The key to get its mapped value
     * @return An optional that might contain the value, or the key's default value.
     */
    public <T> Optional<T> get(Key<T> key) {
        return Optional.ofNullable((T)customData.getOrDefault(key,key.getDefault()));
    }

    public <T> T getOrUse(Key<T> key, T def) {
        if (customData.containsKey(key)) return (T) customData.get(key);
        customData.put(key,def);
        return def;
    }

    public <T> T getOrDefault(Key<T> key) {
        if (customData.containsKey(key)) return (T) customData.get(key);
        T def = key.getDefault();
        customData.put(key,def);
        return def;
    }
}
