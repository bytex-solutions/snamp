package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.io.SerializableMap;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;


abstract class ModifiableMap<K, V> extends HashMap<K, V> implements Externalizable, Modifiable, Stateful, SerializableMap<K, V> {
    private static final long serialVersionUID = -8689048750446731607L;
    private transient boolean modified = false;

    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean isModified() {
        return modified;
    }

    @Override
    public final V remove(@Nonnull final Object key) {
        final V removedValue = super.remove(key);
        markAsModified(removedValue != null);
        return removedValue;
    }

    @Override
    public final boolean remove(final Object key, final Object value) {
        final boolean removed = super.remove(key, value);
        markAsModified(removed);
        return removed;
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        final boolean replaced = super.replace(key, oldValue, newValue);
        markAsModified(replaced);
        return replaced;
    }

    @Override
    public final V replace(final K key, final V value) {
        final V replaced = super.replace(key, value);
        markAsModified(replaced != null);
        return replaced;
    }

    @Override
    public final void replaceAll(final BiFunction<? super K, ? super V, ? extends V> function) {
        super.replaceAll(function);
        markAsModified();
    }

    final void markAsModified(){
        modified = true;
    }

    final void markAsModified(final boolean value) {
        modified |= value;
    }

    @Override
    public void clear() {
        final boolean isNotEmpty = !isEmpty();
        super.clear();
        markAsModified(isNotEmpty);
    }

    @Override
    public final V putIfAbsent(final K key, final V value) {
        final V result = super.putIfAbsent(key, value);
        markAsModified(result == null);
        return result;
    }

    @Override
    public final V put(@Nonnull final K key, @Nonnull final V value) {
        final V previousValue = super.put(key, value);
        markAsModified(!Objects.equals(value, previousValue));
        return previousValue;
    }

    @Override
    public final void putAll(@Nonnull final Map<? extends K, ? extends V> map) {
        if (!map.isEmpty()) {
            markAsModified();
            super.putAll(map);
        }
    }

    @Override
    public final V merge(final K key, final V value, final BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final V oldValue = get(key);
        final V newValue = super.merge(key, value, remappingFunction);
        //If value was changed after merge then mark this map as modified
        markAsModified(!Objects.equals(oldValue, newValue));
        return newValue;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void reset() {
        modified = false;
    }

    protected abstract void writeKey(final K key, final ObjectOutput out) throws IOException;

    protected abstract void writeValue(final V value, final ObjectOutput out) throws IOException;

    protected abstract K readKey(final ObjectInput in) throws IOException;

    protected abstract V readValue(final ObjectInput in) throws IOException, ClassNotFoundException;

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(size());
        for (final Entry<K, V> entry : entrySet()) {
            writeKey(entry.getKey(), out);
            writeValue(entry.getValue(), out);
        }
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        for (int size = in.readInt(); size > 0; size--) {
            final K key = readKey(in);
            final V value = readValue(in);
            if (key != null && value != null)
                put(key, value);
        }
    }
}
