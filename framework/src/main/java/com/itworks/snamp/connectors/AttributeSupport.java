package com.itworks.snamp.connectors;

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
     */
    public AttributeMetadata connectAttribute(final String id, final String attributeName, final Map<String, String> options);

    /**
     * Returns the attribute value.
     * @param id  A key string that is used to invoke attribute from this connector.
     * @param readTimeout The attribute value invoke operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws java.util.concurrent.TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    public Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException;

    /**
     * Reads a set of managementAttributes.
     * @param output The dictionary with set of attribute keys to invoke and associated default values.
     * @param readTimeout The attribute value invoke operation timeout.
     * @return The set of managementAttributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be invoke in the specified duration.
     */
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException;

    /**
     * Writes the value of the specified attribute.
     * @param id An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws TimeoutException The attribute value cannot be write in the specified duration.
     */
    public boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException;

    /**
     * Writes a set of managementAttributes inside of the transaction.
     * @param values The dictionary of managementAttributes keys and its values.
     * @param writeTimeout
     * @return {@literal null}, if the transaction is committed; otherwise, {@literal false}.
     * @throws TimeoutException
     */
    public boolean setAttributes(final Map<String, Object> values, final TimeSpan writeTimeout) throws TimeoutException;

    /**
     * Removes the attribute from the connector.
     * @param id The unique identifier of the attribute.
     * @return {@literal true}, if the attribute successfully disconnected; otherwise, {@literal false}.
     */
    public boolean disconnectAttribute(final String id);

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    public AttributeMetadata getAttributeInfo(final String id);

    /**
     * Returns a read-only collection of registered IDs of managementAttributes.
     * @return A read-only collection of registered IDs of managementAttributes.
     */
    public Collection<String> getConnectedAttributes();
}
