package com.itworks.snamp.connectors.jmx;

import javax.management.openmbean.OpenType;

/**
 * Represents JMX management entity type that can be described as Open MBean type,
 * @author Roman Sakno
 * @param <T> Type of the JMX management entity.
 * @version 1.0
 * @since 1.0
 */
interface JmxManagementEntityOpenType<T> extends JmxManagementEntityType {
    /**
     * Returns Open MBean type associated with this management entity type.
     * @return JMX-compliant representation of the well-known type.
     */
    @SuppressWarnings("UnusedDeclaration")
    public OpenType<T> getOpenType();
}
