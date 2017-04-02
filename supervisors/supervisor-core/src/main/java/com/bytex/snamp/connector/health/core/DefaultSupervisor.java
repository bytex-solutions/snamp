package com.bytex.snamp.connector.health.core;

import com.bytex.snamp.supervision.AbstractSupervisor;
import com.bytex.snamp.connector.health.health.DefaultHealthStatusProvider;

/**
 * Represents default supervisor with health check support and without elasticity management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class DefaultSupervisor extends AbstractSupervisor {
    private final DefaultHealthStatusProvider healthStatusProvider;

    public DefaultSupervisor(final String groupName){
        super(groupName);
        healthStatusProvider = new DefaultHealthStatusProvider();
    }
}
