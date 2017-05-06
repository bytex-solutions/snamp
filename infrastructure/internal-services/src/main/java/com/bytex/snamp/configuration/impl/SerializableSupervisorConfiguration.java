package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.moa.ReduceOperation;
import com.google.common.collect.Range;

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
        protected String readKey(final ObjectInput in) throws IOException {
            return in.readUTF();
        }

        @Override
        protected SerializableScriptletConfiguration readValue(final ObjectInput in) throws IOException, ClassNotFoundException {
            final SerializableScriptletConfiguration result = createValue();
            result.readExternal(in);
            return result;
        }

        @Override
        @Nonnull
        SerializableScriptletConfiguration createValue() {
            return new SerializableScriptletConfiguration();
        }

        void load(final Map<String, ? extends ScriptletConfiguration> checkers) {
            load(checkers, SerializableScriptletConfiguration::load);
        }
    }

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

    private static final class SerializableCustomPolicyConfiguration extends SerializableScriptletConfiguration implements CustomScalingPolicyConfiguration, Modifiable, Stateful, Externalizable{
        private static final long serialVersionUID = 7712023415003133834L;
        private double voteWeight;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableCustomPolicyConfiguration() {
            voteWeight = 0D;
        }

        @Override
        public void setVoteWeight(final double value) {
            voteWeight = value;
            markAsModified();
        }

        @Override
        public double getVoteWeight() {
            return voteWeight;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            super.writeExternal(out);
            out.writeDouble(voteWeight);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            super.readExternal(in);
            voteWeight = in.readDouble();
        }

        @Override
        public int hashCode() {
            return super.hashCode() ^ Double.hashCode(voteWeight);
        }

        private boolean equals(final CustomScalingPolicyInfo other){
            return super.equals(other) && voteWeight == other.getVoteWeight();
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof CustomScalingPolicyInfo && equals((CustomScalingPolicyInfo) other);
        }

        void load(final CustomScalingPolicyInfo other){
            super.load(other);
            voteWeight = other.getVoteWeight();
        }
    }

    private static final class SerializableCustomPolicies extends SerializableFactoryMap<String, SerializableCustomPolicyConfiguration> {
        private static final long serialVersionUID = -7364456833482020352L;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableCustomPolicies() {
        }

        @Nonnull
        @Override
        SerializableCustomPolicyConfiguration createValue() {
            return new SerializableCustomPolicyConfiguration();
        }

        @Override
        protected void writeKey(final String key, final ObjectOutput out) throws IOException {
            out.writeUTF(key);
        }

        @Override
        protected void writeValue(final SerializableCustomPolicyConfiguration value, final ObjectOutput out) throws IOException {
            value.writeExternal(out);
        }

        @Override
        protected String readKey(final ObjectInput in) throws IOException {
            return in.readUTF();
        }

        @Override
        protected SerializableCustomPolicyConfiguration readValue(final ObjectInput in) throws IOException, ClassNotFoundException {
            final SerializableCustomPolicyConfiguration result = createValue();
            result.readExternal(in);
            return result;
        }

        void load(final Map<String, ? extends CustomScalingPolicyInfo> other){
            load(other, SerializableCustomPolicyConfiguration::load);
        }
    }

    private static final class SerializableMetricBasedPolicyConfiguration implements MetricBasedScalingPolicyConfiguration, Modifiable, Stateful, Externalizable{
        private static final long serialVersionUID = 5519650627083210032L;
        private transient boolean modified;
        private double voteWeight;
        private ReduceOperation operation;
        private Range<Double> operationalRange;
        private Duration observationTime;
        private String attributeName;
        private boolean incrementalVoteWeight;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableMetricBasedPolicyConfiguration() {
            voteWeight = 0D;
            operation = ReduceOperation.MAX;
            observationTime = Duration.ZERO;
            attributeName = "";
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
        public void setVoteWeight(final double value) {
            voteWeight = value;
            modified = true;
        }

        @Override
        public void setAggregationMethod(@Nonnull final ReduceOperation value) {
            operation = value;
            modified = true;
        }

        @Override
        public void setRange(@Nonnull final Range<Double> value) {
            operationalRange = value;
            modified = true;
        }

        @Override
        public void setObservationTime(@Nonnull final Duration value) {
            observationTime = value;
            modified = true;
        }

        @Override
        public void setAttributeName(@Nonnull final String value) {
            attributeName = nullToEmpty(value);
            modified = true;
        }

        @Override
        public void setIncrementalVoteWeight(final boolean value) {
            incrementalVoteWeight = value;
            modified = true;
        }

        @Override
        public double getVoteWeight() {
            return voteWeight;
        }

        @Override
        public boolean isIncrementalVoteWeight() {
            return incrementalVoteWeight;
        }

        @Nonnull
        @Override
        public ReduceOperation getAggregationMethod() {
            return operation;
        }

        @Nonnull
        @Override
        public Range<Double> getRange() {
            return operationalRange;
        }

        @Override
        public Duration getObservationTime() {
            return observationTime;
        }

        @Override
        public String getAttributeName() {
            return attributeName;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeDouble(voteWeight);
            out.writeObject(operation);
            out.writeObject(operationalRange);
            out.writeObject(observationTime);
            out.writeUTF(attributeName);
            out.writeBoolean(incrementalVoteWeight);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            voteWeight = in.readDouble();
            operation = (ReduceOperation) in.readObject();
            operationalRange = (Range<Double>) in.readObject();
            observationTime = (Duration) in.readObject();
            attributeName = in.readUTF();
            incrementalVoteWeight = in.readBoolean();
        }

        void load(final MetricBasedScalingPolicyInfo other){
            voteWeight = other.getVoteWeight();
            operation = other.getAggregationMethod();
            operationalRange = other.getRange();
            observationTime = other.getObservationTime();
            attributeName = other.getAttributeName();
            incrementalVoteWeight = other.isIncrementalVoteWeight();
        }
    }

    private static final class SerializableMetricBasedPolicies extends SerializableFactoryMap<String, SerializableMetricBasedPolicyConfiguration>{
        private static final long serialVersionUID = 1852125901504487138L;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableMetricBasedPolicies() {
        }

        @Nonnull
        @Override
        SerializableMetricBasedPolicyConfiguration createValue() {
            return new SerializableMetricBasedPolicyConfiguration();
        }

        @Override
        protected void writeKey(final String key, final ObjectOutput out) throws IOException {
            out.writeUTF(key);
        }

        @Override
        protected void writeValue(final SerializableMetricBasedPolicyConfiguration value, final ObjectOutput out) throws IOException {
            value.writeExternal(out);
        }

        @Override
        protected String readKey(final ObjectInput in) throws IOException {
            return in.readUTF();
        }

        @Override
        protected SerializableMetricBasedPolicyConfiguration readValue(final ObjectInput in) throws IOException, ClassNotFoundException {
            final SerializableMetricBasedPolicyConfiguration result = createValue();
            result.readExternal(in);
            return result;
        }

        void load(final Map<String, ? extends MetricBasedScalingPolicyInfo> other) {
            load(other, SerializableMetricBasedPolicyConfiguration::load);
        }
    }

    final static class SerializableAutoScalingConfiguration implements AutoScalingConfiguration, Modifiable, Stateful, Externalizable{
        private static final long serialVersionUID = 972896691097935578L;
        private transient boolean modified;
        private boolean enabled;
        private Duration cooldownTime;
        private int scalingSize;
        private final SerializableMetricBasedPolicies metricBasedPolicies;
        private final SerializableCustomPolicies customPolicites;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public SerializableAutoScalingConfiguration() {
            cooldownTime = Duration.ZERO;
            scalingSize = 1;
            metricBasedPolicies = new SerializableMetricBasedPolicies();
            customPolicites = new SerializableCustomPolicies();
        }

        @Override
        public boolean isModified() {
            return modified || metricBasedPolicies.isModified() || customPolicites.isModified();
        }

        @Override
        public void reset() {
            metricBasedPolicies.reset();
            customPolicites.reset();
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
            if(value < 1)
                throw new IllegalArgumentException("Scaling size cannot be less than 1");
            scalingSize = value;
            modified = true;
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
        public SerializableMetricBasedPolicies getMetricBasedPolicies() {
            return metricBasedPolicies;
        }

        @Nonnull
        @Override
        public SerializableCustomPolicies getCustomPolicies() {
            return customPolicites;
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeBoolean(enabled);
            out.writeObject(cooldownTime);
            out.writeInt(scalingSize);
            metricBasedPolicies.writeExternal(out);
            customPolicites.writeExternal(out);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            enabled = in.readBoolean();
            cooldownTime = (Duration) in.readObject();
            scalingSize = in.readInt();
            metricBasedPolicies.readExternal(in);
            customPolicites.readExternal(in);
        }

        void clear() {
            enabled = false;
            cooldownTime = Duration.ZERO;
            scalingSize = 1;
            metricBasedPolicies.clear();
            customPolicites.clear();
        }

        void load(final AutoScalingInfo autoScalingConfig) {
            enabled = autoScalingConfig.isEnabled();
            cooldownTime = autoScalingConfig.getCooldownTime();
            scalingSize = autoScalingConfig.getScalingSize();
            metricBasedPolicies.load(autoScalingConfig.getMetricBasedPolicies());
            customPolicites.load(autoScalingConfig.getCustomPolicies());
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
        public SerializableAttributeCheckers getAttributeCheckers() {
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
