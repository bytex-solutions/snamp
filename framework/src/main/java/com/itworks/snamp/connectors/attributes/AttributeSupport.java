package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.TimeSpan;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents support for management managementAttributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeSupport {
    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to invoke attribute from this connector.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @return The description of the attribute.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options) throws AttributeSupportException;

    /**
     * Returns the attribute value.
     * @param id  A key string that is used to invoke attribute from this connector.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The value of the attribute, or default value.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException The requested attribute doesn't exist.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    Object getAttribute(final String id, final TimeSpan readTimeout) throws TimeoutException,
                                                                                UnknownAttributeException,
                                                                                AttributeSupportException;

    /**
     * Reads a set of managementAttributes.
     * @param output The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException, AttributeSupportException;

    /**
     * Writes the value of the specified attribute.
     * @param id An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
     * @throws TimeoutException The attribute value cannot be written in the specified time constraint.
     * @throws com.itworks.snamp.connectors.attributes.UnknownAttributeException The updated attribute doesn't exist.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    void setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException, UnknownAttributeException, AttributeSupportException;

    /**
     * Writes a set of managementAttributes inside of the transaction.
     * @param values The dictionary of managementAttributes keys and its values.
     * @param writeTimeout The attribute value write operation timeout.
     * @throws TimeoutException The attribute value cannot be written in the specified time constraint.
     * @throws com.itworks.snamp.connectors.attributes.AttributeSupportException Internal connector error.
     */
    void setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException, AttributeSupportException;

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    boolean disconnectAttribute(final String id);

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    AttributeMetadata getAttributeInfo(final String id);

    /**
     * Returns a read-only collection of registered IDs of managementAttributes.
     * @return A read-only collection of registered IDs of managementAttributes.
     */
    Collection<String> getConnectedAttributes();
}
