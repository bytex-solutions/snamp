package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents map of configuration parameters.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
class ParametersMap extends ModifiableMap<String, String> {
    private static final long serialVersionUID = 4515173445967181630L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ParametersMap() {
    }

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
