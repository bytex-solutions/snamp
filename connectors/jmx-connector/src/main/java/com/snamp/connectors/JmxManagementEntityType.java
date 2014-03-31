package com.snamp.connectors;

/**
 * Represents JMX-specific management entity type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JmxManagementEntityType extends ManagementEntityType {
    /**
     * Converts well-known management entity value into JMX-specific value.
     * @param value
     * @return
     */
    public Object convertToJmxType(final Object value);
}
