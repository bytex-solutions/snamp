package com.snamp.connectors.util;

import com.snamp.*;
import com.snamp.connectors.*;

import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Represents registry of exposed attributes based on attribute namespace.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class AbstractAttributesRegistry extends HashMap<String, ConnectedAttributes> implements AttributesRegistry {

    /**
     * Initializes a new empty registry of attributes.
     */
    protected AbstractAttributesRegistry(){
        super(10);
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected abstract ConnectedAttributes createBinding(final ManagementConnector connector);

    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.EXCLUSIVE_LOCK)
    @Override
    public final Collection<String> putAll(final ManagementConnector connector, final String prefix, final Map<String, AttributeConfiguration> attributes){
        final ConnectedAttributes binding;
        if(containsKey(prefix))
            binding = get(prefix);
        else put(prefix, binding = createBinding(connector));
        final Collection<String> connectedAttributes = new HashSet<>(attributes.size());
        for(final String postfix: attributes.keySet()){
            final AttributeConfiguration attributeConfig = attributes.get(postfix);
            final AttributeMetadata md = connector.connectAttribute(binding.makeAttributeId(prefix, postfix), attributeConfig.getAttributeName(), attributeConfig.getAdditionalElements());
            if(md != null) {
                binding.put(postfix, md);
                connectedAttributes.add(postfix);
            }
        }
        return connectedAttributes;
    }

    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    @Override
    public final <T> T getAttribute(final String prefix, final String postfix, final Class<T> attributeType, final T defaultValue, final TimeSpan readTimeout){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            if(binding.containsKey(postfix))
                try {
                    final TypeConverter<T> converter = binding.get(postfix).getAttributeType().getProjection(attributeType);
                    return converter != null ?
                            converter.convertFrom(binding.getConnector().getAttribute(binding.makeAttributeId(prefix, postfix), readTimeout, defaultValue)):
                            defaultValue;
                }
                catch (final TimeoutException e) {
                    return defaultValue;
                }
        }
        return defaultValue;
    }

    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    @Override
    public final AttributeValue getAttribute(final String prefix, final String postfix, final TimeSpan readTimeout){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            if(binding.containsKey(postfix))
                try {
                    final ManagementEntityType attributeType = binding.get(postfix).getAttributeType();
                    final Object value = binding.getConnector().getAttribute(binding.makeAttributeId(prefix, postfix), readTimeout, null);
                    return new AttributeValue(value, attributeType);
                }
                catch (final TimeoutException e) {
                    return null;
                }
        }
        return null;
    }

    @Override
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    public final ManagementEntityType getAttributeType(final String prefix, final String postfix){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            return binding.containsKey(postfix) ? binding.get(postfix).getAttributeType() : null;
        }
        else return null;
    }

    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.EXCLUSIVE_LOCK)
    @Override
    public final boolean setAttribute(final String prefix, final String postfix, final Object value, final TimeSpan writeTimeout){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            if(binding.containsKey(postfix))
                try {
                    return binding.getConnector().setAttribute(binding.makeAttributeId(prefix, postfix), writeTimeout, value);
                }
                catch (final TimeoutException e) {
                    return false;
                }
        }
        return false;
    }

    /**
     * Gets a invoke-only collection of registered namespaces.
     *
     * @return A invoke-only collection of registered namespaces.
     */
    @Override
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    public final Collection<String> getNamespaces() {
        return keySet();
    }

    /**
     * Gets a invoke-only collection of registered attributes inside of the specified attributes.
     *
     * @param namespace The attribute namespace.
     * @return A collection of registered attributes located in the specified namespace.
     */
    @Override
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.READ_LOCK)
    public final Collection<String> getRegisteredAttributes(final String namespace) {
        return containsKey(namespace) ? get(namespace).keySet() : Arrays.<String>asList();
    }
}
