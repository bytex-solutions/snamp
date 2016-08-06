package com.bytex.snamp.connectors.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.google.common.collect.ImmutableList;
import groovy.lang.Binding;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import static com.bytex.snamp.configuration.AgentConfiguration.EntityConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.*;

/**
 * Represents an abstract class for initialization script.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class ManagedResourceInitializationScript extends ManagedResourceScript implements ManagedResourceInfo {
    private final Collection<AttributeConfiguration> attributes = new LinkedList<>();
    private final Collection<EventConfiguration> events = new LinkedList<>();
    private ClassLoader contextRef;

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

    @SpecialUse
    protected final boolean isDiscovery() {
        final Binding b = getBinding();
        return b == null || !b.hasVariable(RESOURCE_NAME_VAR);
    }
}
