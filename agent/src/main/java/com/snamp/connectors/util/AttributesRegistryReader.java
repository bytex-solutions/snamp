package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.*;

import java.util.Collection;

/**
 * Represents utility interface for easy reading of connected attributes.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public interface AttributesRegistryReader {
    /**
     * Gets a invoke-only collection of registered namespaces.
     * @return A invoke-only collection of registered namespaces.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> getNamespaces();

    /**
     * Gets a invoke-only collection of registered attributes inside of the specified attributes.
     * @param namespace A namespace of registered attributes.
     * @return A collection of registered attributes in the specified namespace.
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
