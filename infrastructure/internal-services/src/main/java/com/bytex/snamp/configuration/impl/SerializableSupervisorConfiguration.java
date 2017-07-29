package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;

import javax.annotation.Nonnull;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 * Represents serializable configuration of the supervisor.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SerializableSupervisorConfiguration extends AbstractEntityConfiguration implements SupervisorConfiguration {
    private static final long serialVersionUID = 976676415940627332L;

    final static class SerializableDiscoveryConfiguration implements ResourceDiscoveryConfiguration, Modifiable, Stateful, Externalizable{
        private static final long serialVersionUID = -2331867913948707000L;
        private transient boolean modified;
        private String template;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableDiscoveryConfiguration() {
            template = "";
        }

        void clear(){
            template = "";
            modified = true;
        }

        void load(final ResourceDiscoveryInfo other){
            setConnectionStringTemplate(other.getConnectionStringTemplate());
        }

        @Override
        public boolean isModified() {
            return modified;
        }

        @Override
        public void reset() {
            modified = false;
        }

        @Override
        public String getConnectionStringTemplate() {
            return template;
        }

        @Override
        public void setConnectionStringTemplate(final String value) {
            template = nullToEmpty(value);
            modified = true;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeUTF(template);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException {
            template = in.readUTF();
        }

        @Override
        public int hashCode() {
            return template.hashCode();
        }

        private boolean equals(final ResourceDiscoveryConfiguration other) {
            return other.getConnectionStringTemplate().equals(getConnectionStringTemplate());
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof ResourceDiscoveryConfiguration && equals((ResourceDiscoveryConfiguration) other);
        }
    }

    final static class SerializableAutoScalingConfiguration implements AutoScalingConfiguration, Modifiable, Stateful, Externalizable {
        private static final long serialVersionUID = 972896691097935578L;
        private transient boolean modified;
        private boolean enabled;
        private Duration cooldownTime;
        private int scalingSize;
        private int minSize;
        private int maxSize;
        private final SerializableScriptlets policies;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableAutoScalingConfiguration() {
            cooldownTime = Duration.ZERO;
            scalingSize = 1;
            minSize = 0;
            maxSize = Integer.MAX_VALUE;
            policies = new SerializableScriptlets();
        }

        @Override
        public boolean isModified() {
            return modified || policies.isModified();
        }

        @Override
        public void reset() {
            policies.reset();
            modified = false;
        }

        @Override
        public void setEnabled(final boolean value) {
            enabled = value;
            modified = true;
        }

        @Override
        public void setCooldownTime(@Nonnull final Duration value) {
            cooldownTime = Objects.requireNonNull(value);
            modified = true;
        }

        @Override
        public void setScalingSize(final int value) {
            if (value < 1)
                throw new IllegalArgumentException("Scaling size cannot be less than 1");
            scalingSize = value;
            modified = true;
        }

        @Override
        public void setMaxClusterSize(final int value) {
            if(value < 1)
                throw new IllegalArgumentException("Maximum cluster size cannot be less than 1");
            maxSize = value;
            modified = true;
        }

        @Override
        public void setMinClusterSize(final int value) {
            if(value < 0)
                throw new IllegalArgumentException("Minimum cluster size cannot be less than 0");
            minSize = value;
            modified = true;
        }

        @Override
        public int getMaxClusterSize() {
            return maxSize;
        }

        @Override
        public int getMinClusterSize() {
            return minSize;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Nonnull
        @Override
        public Duration getCooldownTime() {
            return cooldownTime;
        }

        @Override
        public int getScalingSize() {
            return scalingSize;
        }

        @Nonnull
        @Override
        public SerializableScriptlets getPolicies() {
            return policies;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeBoolean(enabled);
            out.writeObject(cooldownTime);
            out.writeInt(scalingSize);
            out.writeInt(minSize);
            out.writeInt(maxSize);
            policies.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            enabled = in.readBoolean();
            cooldownTime = (Duration) in.readObject();
            scalingSize = in.readInt();
            minSize = in.readInt();
            maxSize = in.readInt();
            policies.readExternal(in);
        }

        void clear() {
            enabled = false;
            cooldownTime = Duration.ZERO;
            scalingSize = 1;
            minSize = 0;
            maxSize = Integer.MAX_VALUE;
            policies.clear();
        }

        void load(final AutoScalingInfo autoScalingConfig) {
            enabled = autoScalingConfig.isEnabled();
            cooldownTime = autoScalingConfig.getCooldownTime();
            scalingSize = autoScalingConfig.getScalingSize();
            policies.load(autoScalingConfig.getPolicies());
            maxSize = autoScalingConfig.getMaxClusterSize();
            minSize = autoScalingConfig.getMinClusterSize();
        }
    }

    final static class SerializableHealthCheckConfiguration implements HealthCheckConfiguration, Modifiable, Stateful, Externalizable{
        private static final long serialVersionUID = -4851867914948707006L;
        private final SerializableScriptlets checkers;
        private final SerializableScriptletConfiguration trigger;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableHealthCheckConfiguration(){
            checkers = new SerializableScriptlets();
            trigger = new SerializableScriptletConfiguration();
        }

        @Override
        public boolean isModified() {
            return checkers.isModified() || trigger.isModified();
        }

        void load(final HealthCheckInfo configuration){
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
        public SerializableScriptlets getAttributeCheckers() {
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

        void clear() {
            checkers.clear();
            ScriptletConfiguration.fillByDefault(trigger);
        }
    }

    private SerializableHealthCheckConfiguration healthCheckConfig;
    private SerializableDiscoveryConfiguration discoveryConfig;
    private String supervisorType;
    private SerializableAutoScalingConfiguration autoScalingConfig;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableSupervisorConfiguration() {
        healthCheckConfig = new SerializableHealthCheckConfiguration();
        discoveryConfig = new SerializableDiscoveryConfiguration();
        supervisorType = DEFAULT_TYPE;
        autoScalingConfig = new SerializableAutoScalingConfiguration();
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
        markAsModified();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(supervisorType);
        healthCheckConfig.writeExternal(out);
        discoveryConfig.writeExternal(out);
        autoScalingConfig.writeExternal(out);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        supervisorType = in.readUTF();
        healthCheckConfig = new SerializableHealthCheckConfiguration();
        healthCheckConfig.readExternal(in);
        discoveryConfig = new SerializableDiscoveryConfiguration();
        discoveryConfig.readExternal(in);
        autoScalingConfig = new SerializableAutoScalingConfiguration();
        autoScalingConfig.readExternal(in);
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

    @Nonnull
    @Override
    public SerializableDiscoveryConfiguration getDiscoveryConfig() {
        return discoveryConfig;
    }

    /**
     * Gets configuration of elasticity management process.
     *
     * @return Elasticity manager.
     */
    @Nonnull
    @Override
    public SerializableAutoScalingConfiguration getAutoScalingConfig() {
        return autoScalingConfig;
    }

    void setHealthCheckConfig(@Nonnull final SerializableHealthCheckConfiguration value){
        healthCheckConfig = Objects.requireNonNull(value);
    }

    void setDiscoveryConfig(@Nonnull final SerializableDiscoveryConfiguration value){
        discoveryConfig = Objects.requireNonNull(value);
    }

    void setAutoScalingConfig(@Nonnull final SerializableAutoScalingConfiguration value){
        autoScalingConfig = Objects.requireNonNull(value);
    }

    @Override
    public void clear() {
        super.clear();
        healthCheckConfig.clear();
        discoveryConfig.clear();
        autoScalingConfig.clear();
    }

    @Override
    public boolean isModified() {
        return super.isModified() || healthCheckConfig.isModified() || discoveryConfig.isModified() || autoScalingConfig.isModified();
    }

    @Override
    public void reset() {
        super.reset();
        healthCheckConfig.reset();
        discoveryConfig.reset();
        autoScalingConfig.reset();
    }

    private void loadParameters(final Map<String, String> parameters) {
        super.clear();
        super.putAll(parameters);
    }

    private void load(final SupervisorInfo other) {
        healthCheckConfig.load(other.getHealthCheckConfig());
        discoveryConfig.load(other.getDiscoveryConfig());
        autoScalingConfig.load(other.getAutoScalingConfig());
        loadParameters(other);
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if(parameters instanceof SupervisorInfo)
            load((SupervisorInfo) parameters);
        else
            loadParameters(parameters);
    }

    private boolean equals(final SupervisorInfo other) {
        return other.getHealthCheckConfig().equals(healthCheckConfig) &&
                other.getDiscoveryConfig().equals(discoveryConfig) &&
                other.getAutoScalingConfig().equals(autoScalingConfig) &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof SupervisorInfo && equals((SupervisorInfo) other);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(healthCheckConfig, discoveryConfig);
    }
}
