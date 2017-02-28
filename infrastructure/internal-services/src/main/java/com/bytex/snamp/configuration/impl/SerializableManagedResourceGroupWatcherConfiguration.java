package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.FactoryMap;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceGroupWatcherConfiguration;
import com.bytex.snamp.configuration.ScriptletConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;

/**
 * Represents serializable configuration of group watcher.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SerializableManagedResourceGroupWatcherConfiguration extends AbstractEntityConfiguration implements ManagedResourceGroupWatcherConfiguration {
    private static final long serialVersionUID = 976676415940627332L;

    private static final class AttributeCheckers extends SerializableFactoryMap<String, SerializableScriptletConfiguration>{
        private static final long serialVersionUID = 1271232408585113127L;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public AttributeCheckers() {
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
        protected String readKey(final ObjectInput out) throws IOException {
            return out.readUTF();
        }

        @Override
        protected SerializableScriptletConfiguration readValue(final ObjectInput out) throws IOException, ClassNotFoundException {
            final SerializableScriptletConfiguration result = new SerializableScriptletConfiguration();
            result.readExternal(out);
            return result;
        }

        @Override
        SerializableScriptletConfiguration createValue() {
            return new SerializableScriptletConfiguration();
        }

        void load(final Map<String, ? extends ScriptletConfiguration> checkers) {
            load(checkers, SerializableScriptletConfiguration::load);
        }
    }

    private final AttributeCheckers checkers;
    private final SerializableScriptletConfiguration trigger;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableManagedResourceGroupWatcherConfiguration() {
        checkers = new AttributeCheckers();
        trigger = new SerializableScriptletConfiguration();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        checkers.writeExternal(out);
        trigger.writeExternal(out);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        checkers.readExternal(in);
        trigger.readExternal(in);
        super.readExternal(in);
    }

    /**
     * Gets map of attribute checkers where key is attribute name.
     *
     * @return Map of attribute checkers.
     */
    @Override
    public FactoryMap<String, SerializableScriptletConfiguration> getAttributeCheckers() {
        return checkers;
    }

    /**
     * Gets trigger called when status of the component will be changed.
     *
     * @return Trigger configuration.
     */
    @Override
    public SerializableScriptletConfiguration getTrigger() {
        return trigger;
    }

    @Override
    public void clear() {
        super.clear();
        checkers.clear();
    }

    @Override
    public boolean isModified() {
        return super.isModified() || trigger.isModified() || checkers.isModified();
    }

    @Override
    public void reset() {
        super.reset();
        checkers.reset();
        trigger.reset();
    }

    private void loadParameters(final Map<String, String> parameters) {
        super.clear();
        super.putAll(parameters);
    }

    private void load(final ManagedResourceGroupWatcherConfiguration other){
        trigger.load(other.getTrigger());
        checkers.load(other.getAttributeCheckers());
        loadParameters(other);
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if(parameters instanceof ManagedResourceGroupWatcherConfiguration)
            load((ManagedResourceGroupWatcherConfiguration) parameters);
        else
            loadParameters(parameters);
    }

    private boolean equals(final ManagedResourceGroupWatcherConfiguration other) {
        return other.getTrigger().equals(trigger) &&
                checkers.equals(other.getAttributeCheckers());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ManagedResourceGroupWatcherConfiguration && equals((ManagedResourceGroupWatcherConfiguration) other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trigger, checkers);
    }
}
