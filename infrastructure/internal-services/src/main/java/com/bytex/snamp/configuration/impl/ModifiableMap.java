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
        modified = removedValue != null;
        return removedValue;
    }

    final void markAsModified(){
        modified = true;
    }

    public void load(final Map<K, V> values){
        super.clear();
        super.putAll(values);
        markAsModified();
    }

    @Override
    public void clear() {
        markAsModified();
        super.clear();
    }

    @Override
    public final V put(@Nonnull final K key, @Nonnull final V value) {
        markAsModified();
        return super.put(key, value);
    }

    @Override
    public final void putAll(@Nonnull final Map<? extends K, ? extends V> map) {
        if (modified = map.size() > 0)
            super.putAll(map);
    }

    @Override
    public void reset() {
        modified = false;
    }

    protected abstract void writeKey(final K key, final ObjectOutput out) throws IOException;

    protected abstract void writeValue(final V value, final ObjectOutput out) throws IOException;

    protected abstract K readKey(final ObjectInput out) throws IOException;

    protected abstract V readValue(final ObjectInput out) throws IOException, ClassNotFoundException;

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
