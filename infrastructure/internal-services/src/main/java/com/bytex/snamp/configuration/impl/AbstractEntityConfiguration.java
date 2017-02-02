package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.configuration.EntityConfiguration;
import com.bytex.snamp.configuration.EntityMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * Represents abstract class for all serializable configuration entities.
 */
abstract class AbstractEntityConfiguration extends ModifiableMap<String, String> implements SerializableEntityConfiguration {
    private static final long serialVersionUID = -8455277079119895844L;

    @Override
    public abstract void load(final Map<String, String> parameters);

    @Override
    protected final void writeKey(final String key, final ObjectOutput out) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected final void writeValue(final String value, final ObjectOutput out) throws IOException {
        out.writeUTF(value);
    }

    @Override
    protected final String readKey(final ObjectInput out) throws IOException {
        return out.readUTF();
    }

    @Override
    protected final String readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
        return out.readUTF();
    }

    static <T extends EntityConfiguration> void copyEntities(final Map<String, ? extends T> input,
                                                                     final EntityMap<? extends T> output) {
        output.clear();
        for (final Map.Entry<String, ? extends T> entry : input.entrySet()) {
            output.addAndConsume(entry.getValue(), entry.getKey(), (i, o) -> o.load(i));
        }
    }
}
