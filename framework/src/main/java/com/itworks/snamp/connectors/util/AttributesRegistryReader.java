package com.itworks.snamp.connectors.util;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.internal.Internal;
import com.itworks.snamp.internal.MethodThreadSafety;
import com.itworks.snamp.internal.ThreadSafety;

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
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> getNamespaces();

    /**
     * Gets a read-only collection of registered managementAttributes inside of the specified managementAttributes.
     * @param namespace A namespace of registered managementAttributes.
     * @return A collection of registered managementAttributes in the specified namespace.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> getRegisteredAttributes(final String namespace);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public <T> T getAttribute(final String namespace, final String postfix, final Class<T> attributeType, final T defaultValue, final TimeSpan readTimeout);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public AttributeValue<? extends ManagementEntityType> getAttribute(final String namespace, final String postfix, final TimeSpan readTimeout);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public ManagementEntityType getAttributeType(final String namespace, final String postfix);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public boolean setAttribute(final String namespace, final String postfix, final Object value, final TimeSpan writeTimeout);

}
