package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.io.SerializableMap;

import javax.annotation.Nonnull;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;


abstract class ModifiableMap<K, V> extends HashMap<K, V> implements Externalizable, Modifiable, Stateful, SerializableMap<K, V> {
    private static final long serialVersionUID = -8689048750446731607L;
    private transient boolean modified = false;

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public final V remove(@Nonnull final Object key) {
        final V removedValue = super.remove(key);
        if(removedValue != null)
            markAsModified();
        return removedValue;
    }

    @Override
    public final boolean remove(final Object key, final Object value) {
        final boolean removed;
        if (removed = super.remove(key, value))
            markAsModified();
        return removed;
    }

    @Override
    public final boolean replace(final K key, final V oldValue, final V newValue) {
        final boolean replaced;
        if(replaced = super.replace(key, oldValue, newValue))
            markAsModified();
        return replaced;
    }

    @Override
    public final V replace(final K key, final V value) {
        final V replaced = super.replace(key, value);
        if(replaced != null)
            markAsModified();
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

    @Override
    public void clear() {
        markAsModified();
        super.clear();
    }

    @Override
    public final V putIfAbsent(final K key, final V value) {
        final V result = super.putIfAbsent(key, value);
        if(result == null)
            markAsModified();
        return result;
    }

    @Override
    public final V put(@Nonnull final K key, @Nonnull final V value) {
        markAsModified();
        return super.put(key, value);
    }

    @Override
    public final void putAll(@Nonnull final Map<? extends K, ? extends V> map) {
        if (!map.isEmpty()) {
            markAsModified();
            super.putAll(map);
        }
    }

    @Override
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
        final int size = in.readInt();
        for (int i = 0; i < size; i++) {
            final K key = readKey(in);
            final V value = readValue(in);
            if (key != null && value != null)
                put(key, value);
        }
    }
}
