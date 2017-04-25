package com.bytex.snamp.supervision;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.SupervisorInfo;
import com.bytex.snamp.internal.ImmutableEmptyMap;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;

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

    private final EmptyHealthCheckInfo healthCheckConfig;
    private final EmptyResourceDiscoveryInfo discoveryConfig;

    EmptySupervisorInfo(){
        healthCheckConfig = new EmptyHealthCheckInfo();
        discoveryConfig = new EmptyResourceDiscoveryInfo();
    }

    @Nonnull
    @Override
    public EmptyHealthCheckInfo getHealthCheckConfig() {
        return healthCheckConfig;
    }

    @Nonnull
    @Override
    public ResourceDiscoveryInfo getDiscoveryConfig() {
        return discoveryConfig;
    }
}
