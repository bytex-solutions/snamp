package com.bytex.snamp.concurrent.impl;

import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents default immutable configuration of the thread pool.
 */
final class DefaultThreadPoolConfiguration implements Map<String, String>, ThreadPoolConfiguration {
    @Override
    public int getMinPoolSize() {
        return DEFAULT_MIN_POOL_SIZE;
    }

    @Override
    public void setMinPoolSize(final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxPoolSize() {
        return DEFAULT_MAX_POOL_SIZE;
    }

    @Override
    public void setMaxPoolSize(final int value) {

    }

    @Override
    public Duration getKeepAliveTime() {
        return DEFAULT_KEEP_ALIVE_TIME;
    }

    @Override
    public void setKeepAliveTime(final Duration value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getQueueSize() {
        return INFINITE_QUEUE_SIZE;
    }

    @Override
    public void setQueueSize(final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getThreadPriority() {
        return DEFAULT_THREAD_PRIORITY;
    }

    @Override
    public void setThreadPriority(final int value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(final Object key) {
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        return false;
    }

    @Override
    public String get(final Object key) {
        return null;
    }

    @Override
    public String put(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(final Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(final Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Nonnull
    public Set<String> keySet() {
        return ImmutableSet.of();
    }

    @Override
    @Nonnull
    public Collection<String> values() {
        return ImmutableList.of();
    }

    @Override
    @Nonnull
    public Set<Entry<String, String>> entrySet() {
        return ImmutableSet.of();
    }
}
