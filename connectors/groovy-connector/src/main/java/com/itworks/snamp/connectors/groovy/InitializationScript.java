package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.internal.annotations.SpecialUse;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableEventConfiguration;

/**
 * Represents an abstract class for initialization script.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class InitializationScript extends ManagementScript implements AutoCloseable {
    private final Collection<AttributeConfiguration> attributes = new LinkedList<>();
    private final Collection<EventConfiguration> events = new LinkedList<>();

    /**
     * Defines an attribute.
     * @param name The name of the attribute.
     * @param parameters The initial set of configuration parameters
     */
    @SpecialUse
    protected final void attribute(final String name, final Map<String, String> parameters){
        final SerializableAttributeConfiguration config = new SerializableAttributeConfiguration(name);
        config.setParameters(parameters);
        attributes.add(config);
    }

    /**
     * Defines an event.
     * @param category The category of the event.
     * @param parameters The initial set of configuration parameters.
     */
    @SpecialUse
    protected final void event(final String category, final Map<String, String> parameters){
        final SerializableEventConfiguration config = new SerializableEventConfiguration(category);
        config.setParameters(parameters);
        events.add(config);
    }

    @SuppressWarnings("unchecked")
    public final <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType) {
        if(AttributeConfiguration.class.isAssignableFrom(entityType))
            return (Collection<T>)attributes;
        else if(EventConfiguration.class.isAssignableFrom(entityType))
            return (Collection<T>) events;
        else return null;
    }

    /**
     * Releases all resources associated with this script.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        attributes.clear();
        events.clear();
    }
}
