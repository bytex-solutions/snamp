package com.bytex.snamp.configuration.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents abstract class for all serializable configuration entities.
 */
abstract class AbstractEntityConfiguration extends ModifiableMap<String, String> implements SerializableEntityConfiguration {
    private static final long serialVersionUID = -8455277079119895844L;

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
}
