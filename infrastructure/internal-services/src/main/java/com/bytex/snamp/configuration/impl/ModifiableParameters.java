package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;


final class ModifiableParameters extends ModifiableMap<String, String> implements Serializable, Modifiable {
    private static final long serialVersionUID = 6594540590402879949L;
    private final HashMap<String, String> parameters;

    @SpecialUse
    public ModifiableParameters() {
        parameters = new HashMap<>(10);
    }

    @Override
    protected HashMap<String, String> delegate() {
        return parameters;
    }

    @Override
    protected void writeKey(final String key, final ObjectOutput out) throws IOException {
        assert key != null;
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(final String value, final ObjectOutput out) throws IOException {
        out.writeUTF(Objects.requireNonNull(value));
    }

    @Override
    protected String readKey(final ObjectInput out) throws IOException {
        return out.readUTF();
    }

    @Override
    protected String readValue(final ObjectInput out) throws IOException {
        return out.readUTF();
    }
}
