package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.EntityMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Optional;


abstract class SerializableEntityMap<E extends SerializableEntityConfiguration> extends SerializableFactoryMap<String, E> implements EntityMap<E> {
    private static final long serialVersionUID = -3859844548619883398L;

    SerializableEntityMap() {
    }

    final void load(final Map<String, ? extends EntityConfiguration> entities) {
        load(entities, EntityConfiguration::load);
    }

    @Override
    public final Optional<E> getIfPresent(final String entityID) {
        return Optional.ofNullable(get(entityID));
    }

    @Override
    protected final void writeValue(final E value, final ObjectOutput out) throws IOException {
        value.writeExternal(out);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final E readValue(final ObjectInput in) throws IOException, ClassNotFoundException {
        final E result = createValue();
        result.readExternal(in);
        return result;
    }

    @Override
    protected final void writeKey(final String key, final ObjectOutput out) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected final String readKey(final ObjectInput out) throws IOException {
        return out.readUTF();
    }
}
