package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.EntityMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;


abstract class ConfigurationEntityRegistry<E extends EntityConfiguration & Modifiable & Resettable> extends ModifiableMap<String, E> implements EntityMap<E> {
    private static final long serialVersionUID = -3859844548619883398L;
    private final HashMap<String, E> entities;

    ConfigurationEntityRegistry() {
        entities = new HashMap<>(10);
    }

    final <ERROR extends Exception> void modifiedEntries(final EntryReader<String, ? super E, ERROR> reader) throws ERROR {
        for (final Entry<String, E> e : entrySet()) {
            final E entity = e.getValue();
            final String name = e.getKey();
            if (entity.isModified())
                if (!reader.read(name, entity)) break;
        }
    }

    @Override
    public final E getOrAdd(final String entityID) {
        final E result;
        if (containsKey(entityID))
            result = get(entityID);
        else {
            result = createEntity();
            put(entityID, result);
        }
        return result;
    }

    @Override
    protected final void writeValue(final E value, final ObjectOutput out) throws IOException {
        out.writeObject(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final E readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
        return (E) out.readObject();
    }

    @Override
    protected final HashMap<String, E> delegate() {
        return entities;
    }

    @Override
    public final boolean isModified() {
        if (super.isModified()) return true;
        else for (final Modifiable entity : values())
            if (entity.isModified()) return true;
        return false;
    }

    private static void reset(final Resettable r){
        r.reset();
    }

    @Override
    public final void reset() {
        super.reset();
        values().forEach(ConfigurationEntityRegistry::reset);//BANANA: Bug in JDK, can't replace with Resettable::reset
    }

    @Override
    protected final void writeKey(final String key, final ObjectOutput out) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected final String readKey(final ObjectInput out) throws IOException {
        return out.readUTF();
    }

    protected abstract E createEntity();
}
