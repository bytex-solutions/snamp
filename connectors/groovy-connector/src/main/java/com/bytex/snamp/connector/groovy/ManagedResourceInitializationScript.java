package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents an abstract class for initialization script.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ManagedResourceInitializationScript extends Scriptlet implements ManagedResourceInfo, ManagedResourceScript {
    private final Collection<AttributeConfiguration> attributes = new LinkedList<>();
    private final Collection<EventConfiguration> events = new LinkedList<>();
    private ClassLoader contextRef;
    private boolean isDiscovery;

    final void setContext(final ClassLoader context){
        this.contextRef = context;
    }

    private <E extends EntityConfiguration> E createEntityConfiguration(final Class<E> entityType) {
        return contextRef != null ? ConfigurationManager.createEntityConfiguration(contextRef, entityType) : null;
    }

    /**
     * Defines an attribute.
     * @param name The name of the attribute.
     * @param parameters The initial set of configuration parameters
     */
    @SpecialUse
    protected final void attribute(final String name, final Map<String, String> parameters){
        final AttributeConfiguration config = createEntityConfiguration(AttributeConfiguration.class);
        if(config != null) {
            config.setAlternativeName(name);
            config.setParameters(parameters);
            attributes.add(config);
        }
    }

    /**
     * Defines an event.
     * @param category The category of the event.
     * @param parameters The initial set of configuration parameters.
     */
    @SpecialUse
    protected final void event(final String category, final Map<String, String> parameters){
        final EventConfiguration config = createEntityConfiguration(EventConfiguration.class);
        if(config != null) {
            config.setAlternativeName(category);
            config.setParameters(parameters);
            events.add(config);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType) {
        if(AttributeConfiguration.class.isAssignableFrom(entityType))
            return (Collection<T>) ImmutableList.copyOf(attributes);
        else if(EventConfiguration.class.isAssignableFrom(entityType))
            return (Collection<T>) ImmutableList.copyOf(events);
        else return null;
    }

    /**
     * Releases all resources associated with this script.
     * @throws Exception
     */
    @Override
    public void close() throws Exception {
        contextRef = null;
        attributes.clear();
        events.clear();
    }

    final void setDiscovery(final boolean value){
        isDiscovery = value;
    }

    @SpecialUse
    protected final boolean isDiscovery() {
        return isDiscovery;
    }
}
