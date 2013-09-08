package com.snamp.connectors;

import com.snamp.TimeSpan;
import net.xeoh.plugins.base.Plugin;

import java.util.*;
import java.util.concurrent.*;

/**
 * Represents management connector that exposes management attributes of the remote provider.
 * @author roman
 */
public interface ManagementConnector extends Iterable<String>, AutoCloseable, Plugin {
    /**
     * Initialize the management connector.
     * @param connectionString Connection string.
     * @param connectionProperties Connection parameters.
     * @return {@literal true}, if this instance is initialized successfully; otherwise, {@literal false}.
     */
    public boolean initialize(final String connectionString, final Properties connectionProperties);

    /**
     * Connects to the specified attribute.
     * @param id A key string that is used to read attribute from this connector.
     * @param namespace The namespace of the attribute.
     * @param attributeName The name of the attribute.
     * @param options The attribute discovery options.
     * @param tags The set of custom objects associated with the attribute.
     * @return The description of the attribute.
     */
    public AttributeMetadata connectAttribute(final String id, final String namespace, final String attributeName, final AttributeConnectionOptions options, final Set<Object> tags);

    /**
     * Returns the attribute value.
     * @param id  A key string that is used to read attribute from this connector.
     * @param readTimeout The attribute value read operation timeout.
     * @param defaultValue The default value of the attribute if it is real value is not available.
     * @return The value of the attribute, or default value.
     * @throws TimeoutException The attribute value cannot be read in the specified time.
     */
    public Object getAttribute(final String id, final TimeSpan readTimeout, final Object defaultValue) throws TimeoutException;

    /**
     * Reads a set of attributes.
     * @param output The dictionary with set of attribute keys to read and associated default values.
     * @param readTimeout The attribute value read operation timeout.
     * @return The set of attributes ids really written to the dictionary.
     * @throws TimeoutException The attribute value cannot be read in the specified time.
     */
    public Set<String> getAttributes(final Map<String, Object> output, final TimeSpan readTimeout) throws TimeoutException;

    /**
     * Writes the value of the specified attribute.
     * @param id An identifier of the attribute,
     * @param writeTimeout The attribute value write operation timeout.
     * @param value The value to write.
     * @return {@literal true} if attribute set operation is supported by remote provider; otherwise, {@literal false}.
     * @throws TimeoutException The attribute value cannot be write in the specified time.
     */
    public boolean setAttribute(final String id, final TimeSpan writeTimeout, final Object value) throws TimeoutException;

    /**
     * Writes a set of attributes inside of the transaction.
     * @param values The dictionary of attributes keys and its values.
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
    public boolean disconnectAttribute(String id);

    /**
     * Returns the information about the connected attribute.
     * @param id An identifier of the attribute.
     * @return The attribute descriptor; or {@literal null} if attribute is not connected.
     */
    public AttributeMetadata getAttributeInfo(String id);
}
