package com.itworks.snamp.connectors.attributes;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.internal.semantics.ThreadSafe;
import com.itworks.snamp.internal.semantics.Internal;

import java.util.Collection;

/**
 * Represents utility interface for easy reading of connected managementAttributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface AttributesRegistryReader {
    /**
     * Gets a read-only collection of registered namespaces.
     * @return A read-only collection of registered namespaces.
     */
    @ThreadSafe(false)
    public Collection<String> getNamespaces();

    /**
     * Gets a read-only collection of registered managementAttributes inside of the specified managementAttributes.
     * @param namespace A namespace of registered managementAttributes.
     * @return A collection of registered managementAttributes in the specified namespace.
     */
    @ThreadSafe(false)
    public Collection<String> getRegisteredAttributes(final String namespace);

    @ThreadSafe(false)
    public <T> T getAttribute(final String namespace, final String postfix, final Class<T> attributeType, final T defaultValue, final TimeSpan readTimeout);

    @ThreadSafe(false)
    public AttributeValue<? extends ManagementEntityType> getAttribute(final String namespace, final String postfix, final TimeSpan readTimeout);

    @ThreadSafe(false)
    public ManagementEntityType getAttributeType(final String namespace, final String postfix);

    @ThreadSafe(false)
    public boolean setAttribute(final String namespace, final String postfix, final Object value, final TimeSpan writeTimeout);

}
