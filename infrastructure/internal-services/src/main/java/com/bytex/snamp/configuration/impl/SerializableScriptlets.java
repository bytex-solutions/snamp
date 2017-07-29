package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ScriptletConfiguration;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SerializableScriptlets extends SerializableFactoryMap<String, SerializableScriptletConfiguration> {
    private static final long serialVersionUID = 1271232408585113127L;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableScriptlets() {
    }

    @Override
    protected void writeKey(final String key, final ObjectOutput out) throws IOException {
        out.writeUTF(key);
    }

    @Override
    protected void writeValue(final SerializableScriptletConfiguration value, final ObjectOutput out) throws IOException {
        value.writeExternal(out);
    }

    @Override
    protected String readKey(final ObjectInput in) throws IOException {
        return in.readUTF();
    }

    @Override
    protected SerializableScriptletConfiguration readValue(final ObjectInput in) throws IOException, ClassNotFoundException {
        final SerializableScriptletConfiguration result = createValue();
        result.readExternal(in);
        result.reset(); //reset modification state after deserialization
        return result;
    }

    @Override
    @Nonnull
    SerializableScriptletConfiguration createValue() {
        return new SerializableScriptletConfiguration(true);
    }

    void load(final Map<String, ? extends ScriptletConfiguration> checkers) {
        load(checkers, SerializableScriptletConfiguration::load);
    }
}
