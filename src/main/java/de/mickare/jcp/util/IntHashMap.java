/*
 * Copyright 2023 Michael Käser
 *
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of either the Apache License, Version 2.0 or the MIT License.
 *
 * You may obtain a copy of the Apache License, Version 2.0 and the MIT License at
 * <http://www.apache.org/licenses/LICENSE-2.0> and
 * <https://opensource.org/licenses/MIT>, respectively.
 */
package de.mickare.jcp.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class IntHashMap<K> {

    private final Map<K, Integer> delegate = new HashMap<>();

    public IntHashMap() {
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    public int incrementAndGet(K key) {
        return delegate.merge(key, 1, Integer::sum);
    }

    public int get(K key) {
        return delegate.getOrDefault(key, 0);
    }

    private int unbox(@Nullable Integer value) {
        return value != null ? value : 0;
    }

    public int put(K key, int value) {
        return unbox(delegate.put(key, value));
    }

    public int remove(K key) {
        return unbox(delegate.remove(key));
    }

    public void putAll(@NotNull Map<? extends K, ? extends Integer> m) {
        delegate.putAll(m);
    }

    public void clear() {
        delegate.clear();
    }

    @NotNull
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @NotNull
    public Collection<Integer> values() {
        return delegate.values();
    }

    @NotNull
    public Set<Map.Entry<K, Integer>> entrySet() {
        return delegate.entrySet();
    }
}
