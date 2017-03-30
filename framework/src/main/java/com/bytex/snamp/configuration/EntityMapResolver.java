package com.bytex.snamp.configuration;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * Represents functional interface used to extract entity map from its owner.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface EntityMapResolver<I extends EntityConfiguration, O extends EntityConfiguration> extends Function<I, EntityMap<? extends O>> {
    EntityMapResolver<AgentConfiguration, ManagedResourceConfiguration> RESOURCES = AgentConfiguration::getResources;
    EntityMapResolver<AgentConfiguration, ThreadPoolConfiguration> THREAD_POOLS = AgentConfiguration::getThreadPools;
    EntityMapResolver<AgentConfiguration, GatewayConfiguration> GATEWAYS = AgentConfiguration::getGateways;
    EntityMapResolver<AgentConfiguration, ManagedResourceGroupConfiguration> GROUPS = AgentConfiguration::getResourceGroups;
    EntityMapResolver<AgentConfiguration, SupervisorConfiguration> SUPERVISORS = AgentConfiguration::getSupervisors;
    EntityMapResolver<? super ManagedResourceTemplate, AttributeConfiguration> ATTRIBUTES = ManagedResourceTemplate::getAttributes;
    EntityMapResolver<? super ManagedResourceTemplate, EventConfiguration> EVENTS = ManagedResourceTemplate::getEvents;
    EntityMapResolver<? super ManagedResourceTemplate, OperationConfiguration> OPERATIONS = ManagedResourceTemplate::getOperations;

    @Override
    @Nonnull
    EntityMap<? extends O> apply(@Nonnull final I owner);
}
