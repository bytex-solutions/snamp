package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;

import javax.annotation.Nonnull;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;
import static com.google.common.base.Strings.*;

/**
 * Represents serializable configuration of the supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SerializableSupervisorConfiguration extends AbstractEntityConfiguration implements SupervisorConfiguration {
    private static final long serialVersionUID = 976676415940627332L;

    private static final class SerializableAttributeCheckers extends SerializableFactoryMap<String, SerializableScriptletConfiguration>{
        private static final long serialVersionUID = 1271232408585113127L;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableAttributeCheckers() {
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

    final static class SerializableHealthCheckConfiguration implements HealthCheckConfiguration, Modifiable, Stateful, Externalizable{
        private static final long serialVersionUID = -4851867914948707006L;
        private final SerializableAttributeCheckers checkers;
        private final SerializableScriptletConfiguration trigger;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableHealthCheckConfiguration(){
            checkers = new SerializableAttributeCheckers();
            trigger = new SerializableScriptletConfiguration();
        }

        private SerializableHealthCheckConfiguration(final ObjectInput input) throws IOException, ClassNotFoundException {
            this();
            readExternal(input);
        }

        @Override
        public boolean isModified() {
            return checkers.isModified() || trigger.isModified();
        }

        void load(final HealthCheckConfiguration configuration){
            trigger.load(configuration.getTrigger());
            checkers.load(configuration.getAttributeCheckers());
        }

        /**
         * Resets internal state of the object.
         */
        @Override
        public void reset() {
            checkers.reset();
            trigger.reset();
        }

        /**
         * Gets map of attribute checkers where key is attribute name.
         *
         * @return Map of attribute checkers.
         */
        @Override
        @Nonnull
        public SerializableFactoryMap<String, SerializableScriptletConfiguration> getAttributeCheckers() {
            return checkers;
        }

        /**
         * Gets trigger called when status of the component will be changed.
         *
         * @return Trigger configuration.
         */
        @Override
        @Nonnull
        public SerializableScriptletConfiguration getTrigger() {
            return trigger;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            trigger.writeExternal(out);
            checkers.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            trigger.readExternal(in);
            checkers.readExternal(in);
        }

        @Override
        public int hashCode() {
            return Objects.hash(trigger, checkers);
        }

        private boolean equals(final HealthCheckConfiguration other) {
            return other.getTrigger().equals(trigger) &&
                    other.getAttributeCheckers().equals(checkers);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof HealthCheckConfiguration && equals((HealthCheckConfiguration) other);
        }
    }

    private SerializableHealthCheckConfiguration healthCheckConfig;
    private String supervisorType;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableSupervisorConfiguration() {
        healthCheckConfig = new SerializableHealthCheckConfiguration();
        supervisorType = DEFAULT_TYPE;
    }

    /**
     * Gets supervisor type.
     *
     * @return Supervisor type.
     */
    @Override
    public String getType() {
        return isNullOrEmpty(supervisorType) ? DEFAULT_TYPE : supervisorType;
    }

    /**
     * Sets supervisor type.
     *
     * @param value Supervisor type.
     */
    @Override
    public void setType(final String value) {
        supervisorType = nullToEmpty(value);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(supervisorType);
        healthCheckConfig.writeExternal(out);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        supervisorType = in.readUTF();
        healthCheckConfig = new SerializableHealthCheckConfiguration(in);
        super.readExternal(in);
    }

    /**
     * Gets configuration of the health check.
     *
     * @return Configuration of the health checks.
     */
    @Override
    @Nonnull
    public SerializableHealthCheckConfiguration getHealthCheckConfig() {
        return healthCheckConfig;
    }

    void setHealthCheckConfig(@Nonnull final SerializableHealthCheckConfiguration value){
        healthCheckConfig = value;
    }

    @Override
    public void clear() {
        super.clear();
        healthCheckConfig.getAttributeCheckers().clear();
    }

    @Override
    public boolean isModified() {
        return super.isModified() || healthCheckConfig.isModified();
    }

    @Override
    public void reset() {
        super.reset();
        healthCheckConfig.reset();
    }

    private void loadParameters(final Map<String, String> parameters) {
        super.clear();
        super.putAll(parameters);
    }

    private void load(final SupervisorConfiguration other) {
        healthCheckConfig.load(other.getHealthCheckConfig());
        loadParameters(other);
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if(parameters instanceof SupervisorConfiguration)
            load((SupervisorConfiguration) parameters);
        else
            loadParameters(parameters);
    }

    private boolean equals(final SupervisorConfiguration other) {
        return other.getHealthCheckConfig().equals(healthCheckConfig) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof SupervisorConfiguration && equals((SupervisorConfiguration) other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(healthCheckConfig);
    }
}
