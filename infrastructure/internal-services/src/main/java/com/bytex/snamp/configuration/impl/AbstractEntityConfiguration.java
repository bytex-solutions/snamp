package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SerializableMap;
import com.bytex.snamp.Stateful;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * Represents abstract class for all serializable configuration entities.
 */
abstract class AbstractEntityConfiguration implements Stateful, SerializableEntityConfiguration {
    private static final long serialVersionUID = -8455277079119895844L;
    private transient boolean modified;
    private final ModifiableParameters parameters;

    AbstractEntityConfiguration() {
        parameters = new ModifiableParameters();
        modified = false;
    }

    final void writeParameters(final ObjectOutput out) throws IOException {
        parameters.writeExternal(out);
    }

    final void readParameters(final ObjectInput in) throws IOException, ClassNotFoundException {
        parameters.readExternal(in);
    }

    @Override
    public void reset() {
        modified = false;
        parameters.reset();
    }

    @Override
    public final void setParameters(final Map<String, String> value) {
        parameters.importFrom(value);
    }

    final void markAsModified() {
        modified = true;
    }

    /**
     * Determines whether this configuration entity is modified after deserialization.
     *
     * @return {@literal true}, if this configuration entity is modified; otherwise, {@literal false}.
     */
    @Override
    public boolean isModified() {
        return modified || parameters.isModified();
    }

    /**
     * Gets serializable configuration parameters of this entity.
     *
     * @return A map of configuration parameters.
     */
    @Override
    public final SerializableMap<String, String> getParameters() {
        return parameters;
    }

}
