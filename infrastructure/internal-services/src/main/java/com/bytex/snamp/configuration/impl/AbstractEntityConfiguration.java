package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SerializableMap;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.function.*;

/**
 * Represents abstract class for all serializable configuration entities.
 */
abstract class AbstractEntityConfiguration implements Resettable, SerializableEntityConfiguration {
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
    public final void reset() {
        modified = false;
        parameters.reset();
        resetAdditionally();
    }

    final long getLongParameter(final String key, final ToLongFunction<? super String> converter, final long defaultValue){
        return parameters.containsKey(key)? converter.applyAsLong(parameters.get(key)) : defaultValue;
    }

    final void setLongParameter(final String key, final long value, final LongFunction<String> converter){
        parameters.put(key, converter.apply(value));
        markAsModified();
    }

    final int getIntParameter(final String key, final ToIntFunction<? super String> converter, final int defaultValue){
        return parameters.containsKey(key)? converter.applyAsInt(parameters.get(key)) : defaultValue;
    }

    final void setIntParameter(final String key, final int value, final IntFunction<String> converter){
        parameters.put(key, converter.apply(value));
        markAsModified();
    }

    final <P> P getParameter(final String key, final Function<? super String, ? extends P> converter, final P defaultValue){
        return parameters.containsKey(key)? converter.apply(parameters.get(key)) : defaultValue;
    }

    final <P> void setParameter(final String key, final P value, final Function<? super P, String> converter){
        parameters.put(key, converter.apply(value));
        markAsModified();
    }

    @Override
    public final void setParameters(final Map<String, String> value) {
        parameters.clear();
        parameters.putAll(value);
    }

    void resetAdditionally() {

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
