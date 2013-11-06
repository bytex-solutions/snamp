package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.*;

import java.util.Collection;

/**
 * Represents utility interface for easy reading of connected attributes.
 * @author roman
 */
public interface AttributesRegistryReader {
    /**
     * Gets a read-only collection of registered namespaces.
     * @return
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> getNamespaces();

    /**
     * Gets a read-only collection of registered attributes inside of the specified attributes.
     * @param namespace
     * @return
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public Collection<String> getRegisteredAttributes(final String namespace);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public <T> T getAttribute(final String namespace, final String postfix, final Class<T> attributeType, final T defaultValue, final TimeSpan readTimeout);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public AttributeValue<? extends AttributeTypeInfo> getAttribute(final String namespace, final String postfix, final TimeSpan readTimeout);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public AttributeTypeInfo getAttributeType(final String namespace, final String postfix);

    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public boolean setAttribute(final String namespace, final String postfix, final Object value, final TimeSpan writeTimeout);

}
