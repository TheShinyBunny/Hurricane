package com.shinybunny.hurricane.util;

import com.shinybunny.hurricane.Key;

import java.util.*;

public class CustomDataHolder {

    private Map<Key<?>,Object> customData = new HashMap<>();
    private Set<String> flags = new HashSet<>();

    public <T> void set(Key<T> key, T value) {
        this.customData.put(key,value);
    }

    public void addFlag(String flag) {
        flags.add(flag);
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }

    public void remove(Key<?> key) {
        customData.remove(key);
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

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
