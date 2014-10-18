package com.itworks.snamp.connectors.attributes;

/**
 * Represents writer for logs related to operations with attributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeSupportReporter {
    /**
     * Reports about attribute read failure.
     * @param attributeID The identifier of the attribute.
     * @param reason The reason of the failure.
     * @see com.itworks.snamp.connectors.attributes.AttributeSupport#getAttribute(String, com.itworks.snamp.TimeSpan, Object)
     */
    void unableToGetAttribute(final String attributeID, final Exception reason);
}
