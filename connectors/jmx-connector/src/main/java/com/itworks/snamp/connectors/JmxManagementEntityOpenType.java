package com.itworks.snamp.connectors;

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
     * @return
     */
    public OpenType<T> getOpenType();
}
