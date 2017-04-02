package com.bytex.snamp.connector.supervision.core;

import com.bytex.snamp.connector.supervision.AbstractSupervisor;
import com.bytex.snamp.connector.supervision.health.DefaultHealthStatusProvider;

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
