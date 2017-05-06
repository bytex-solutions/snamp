package com.bytex.snamp.supervision;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.internal.ImmutableEmptyMap;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class EmptySupervisorInfo extends ImmutableEmptyMap<String, String> implements SupervisorInfo {
    private static final class EmptyHealthCheckInfo implements HealthCheckInfo{
        @Nonnull
        @Override
        public ImmutableMap<String, ? extends ScriptletConfiguration> getAttributeCheckers() {
            return ImmutableMap.of();
        }

        @Override
        @Nonnull
        public ScriptletConfiguration getTrigger() {
            return ScriptletConfiguration.EMPTY;
        }
    }

    private static final class EmptyResourceDiscoveryInfo implements ResourceDiscoveryInfo{
        @Override
        public String getConnectionStringTemplate() {
            return "";
        }
    }

    private static final class EmptyAutoScalingInfo implements AutoScalingInfo{
        @Override
        public boolean isEnabled() {
            return false;
        }

        @Nonnull
        @Override
        public Duration getCooldownTime() {
            return Duration.ZERO;
        }

        @Override
        public int getScalingSize() {
            return 0;
        }

        @Nonnull
        @Override
        public ImmutableMap<String, ? extends MetricBasedScalingPolicyInfo> getMetricBasedPolicies() {
            return ImmutableMap.of();
        }

        @Nonnull
        @Override
        public ImmutableMap<String, ? extends CustomScalingPolicyInfo> getCustomPolicies() {
            return ImmutableMap.of();
        }
    }

    private final EmptyHealthCheckInfo healthCheckConfig;
    private final EmptyResourceDiscoveryInfo discoveryConfig;
    private final EmptyAutoScalingInfo autoScalingInfo;

    EmptySupervisorInfo(){
        healthCheckConfig = new EmptyHealthCheckInfo();
        discoveryConfig = new EmptyResourceDiscoveryInfo();
        autoScalingInfo = new EmptyAutoScalingInfo();
    }

    @Nonnull
    @Override
    public EmptyHealthCheckInfo getHealthCheckConfig() {
        return healthCheckConfig;
    }

    @Nonnull
    @Override
    public EmptyResourceDiscoveryInfo getDiscoveryConfig() {
        return discoveryConfig;
    }

    @Nonnull
    @Override
    public EmptyAutoScalingInfo getAutoScalingConfig() {
        return autoScalingInfo;
    }

    @Override
    public int hashCode() {
        return 0x012424fa;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof EmptySupervisorInfo;
    }
}
