package com.itworks.snamp.connectors.util;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.TypeConverter;
import com.itworks.snamp.connectors.AttributeMetadata;
import com.itworks.snamp.connectors.AttributeSupport;
import com.itworks.snamp.connectors.ManagementEntityType;
import com.itworks.snamp.internal.semantics.Internal;
import com.itworks.snamp.internal.semantics.ThreadSafe;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

/**
 * Represents registry of exposed managementAttributes based on attribute namespace.
 * @param <TAttributeDescriptor> The type of the attribute descriptor.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
public abstract class AbstractAttributesRegistry<TAttributeDescriptor> extends HashMap<String, ConnectedAttributes<TAttributeDescriptor>> implements AttributesRegistry {

    /**
     * Initializes a new empty registry of managementAttributes.
     */
    protected AbstractAttributesRegistry(){
        super(10);
    }

    @ThreadSafe
    protected abstract ConnectedAttributes<TAttributeDescriptor> createBinding(final AttributeSupport connector);

    @ThreadSafe(false)
    @Override
    public final Collection<String> putAll(final AttributeSupport connector, final String prefix, final Map<String, AttributeConfiguration> attributes){
        final ConnectedAttributes<TAttributeDescriptor> binding;
        if(containsKey(prefix))
            binding = get(prefix);
        else put(prefix, binding = createBinding(connector));
        final Collection<String> connectedAttributes = new HashSet<>(attributes.size());
        for(final String postfix: attributes.keySet()){
            final AttributeConfiguration attributeConfig = attributes.get(postfix);
            final AttributeMetadata md = connector.connectAttribute(binding.makeAttributeId(prefix, postfix), attributeConfig.getAttributeName(), attributeConfig.getAdditionalElements());
            if(md != null) {
                final TAttributeDescriptor descriptor = binding.createDescription(prefix, postfix, attributeConfig);
                if(descriptor != null){
                    binding.put(postfix, descriptor);
                    connectedAttributes.add(postfix);
                }
            }
        }
        return connectedAttributes;
    }

    @ThreadSafe(false)
    @Override
    public final <T> T getAttribute(final String prefix, final String postfix, final Class<T> attributeType, final T defaultValue, final TimeSpan readTimeout){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            if(binding.containsKey(postfix))
                try {
                    final TypeConverter<T> converter = binding.getAttributeType(prefix, postfix).getProjection(attributeType);
                    return converter != null ?
                            converter.convertFrom(binding.getAttribute(binding.makeAttributeId(prefix, postfix), readTimeout, defaultValue)):
                            defaultValue;
                }
                catch (final TimeoutException e) {
                    return defaultValue;
                }
        }
        return defaultValue;
    }

    @ThreadSafe(false)
    @Override
    public final AttributeValue<? extends ManagementEntityType> getAttribute(final String prefix, final String postfix, final TimeSpan readTimeout){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            if(binding.containsKey(postfix))
                try {
                    final ManagementEntityType attributeType = binding.getAttributeType(prefix, postfix);
                    final Object value = binding.getAttribute(binding.makeAttributeId(prefix, postfix), readTimeout, null);
                    return new AttributeValue<>(value, attributeType);
                }
                catch (final TimeoutException e) {
                    return null;
                }
        }
        return null;
    }

    @Override
    @ThreadSafe(false)
    public final ManagementEntityType getAttributeType(final String prefix, final String postfix){
        if(containsKey(prefix)){
            final ConnectedAttributes<TAttributeDescriptor> binding = get(prefix);
            return binding.containsKey(postfix) ? binding.getAttributeType(prefix, postfix) : null;
        }
        else return null;
    }

    @ThreadSafe(false)
    @Override
    public final boolean setAttribute(final String prefix, final String postfix, final Object value, final TimeSpan writeTimeout){
        if(containsKey(prefix)){
            final ConnectedAttributes binding = get(prefix);
            if(binding.containsKey(postfix))
                try {
                    return binding.setAttribute(binding.makeAttributeId(prefix, postfix), writeTimeout, value);
                }
                catch (final TimeoutException e) {
                    return false;
                }
        }
        return false;
    }

    /**
     * Gets a read-only collection of registered namespaces.
     *
     * @return A read-only collection of registered namespaces.
     */
    @Override
    @ThreadSafe(false)
    public final Collection<String> getNamespaces() {
        return keySet();
    }

    /**
     * Gets a read-only collection of registered managementAttributes inside of the specified managementAttributes.
     *
     * @param namespace The attribute namespace.
     * @return A collection of registered managementAttributes located in the specified namespace.
     */
    @Override
    @ThreadSafe(false)
    public final Collection<String> getRegisteredAttributes(final String namespace) {
        return containsKey(namespace) ? get(namespace).keySet() : Arrays.<String>asList();
    }

    public final <T extends ConnectedAttributes> T get(final String prefix, final Class<T> classInfo){
        final ConnectedAttributes result = get(prefix);
        return classInfo.isInstance(result) ? classInfo.cast(result) : null;
    }

    /**
     * Disconnects all managementAttributes.
     */
    @Override
    public final void disconnect() {
        for(final Map.Entry<String, ConnectedAttributes<TAttributeDescriptor>> entry: entrySet())
            entry.getValue().disconnect(entry.getKey());
    }
}
