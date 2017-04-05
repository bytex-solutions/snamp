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

    private final EmptyHealthCheckInfo healthCheckConfig;

    EmptySupervisorInfo(){
        healthCheckConfig = new EmptyHealthCheckInfo();
    }

    @Nonnull
    @Override
    public EmptyHealthCheckInfo getHealthCheckConfig() {
        return healthCheckConfig;
    }
}
