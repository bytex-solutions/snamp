package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.*;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class TemplateDataObject<E extends ManagedResourceTemplate> extends AbstractTypedDataObject<E> {
    private final Map<String, AttributeDataObject> attributes;
    private final Map<String, EventDataObject> events;
    private final Map<String, OperationDataObject> operations;

    TemplateDataObject(){
        this.attributes = new HashMap<>();
        this.events = new HashMap<>();
        this.operations = new HashMap<>();
    }

    TemplateDataObject(final E configuration){
        super(configuration);
        attributes = configuration.getFeatures(AttributeConfiguration.class).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new AttributeDataObject(entry.getValue())));

        events = configuration.getFeatures(EventConfiguration.class).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new EventDataObject(entry.getValue())));

        operations = configuration.getFeatures(OperationConfiguration.class).entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> new OperationDataObject(entry.getValue())));
    }

    /**
     * Gets attributes.
     *
     * @return the attributes
     */
    @JsonProperty
    public final Map<String, AttributeDataObject> getAttributes() {
        return attributes;
    }

    /**
     * Sets attributes.
     *
     * @param attributes the attributes
     */
    public final void setAttributes(final Map<String, AttributeDataObject> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
    }

    /**
     * Gets events.
     *
     * @return the events
     */
    @JsonProperty
    public final Map<String, EventDataObject> getEvents() {
        return events;
    }

    /**
     * Sets events.
     *
     * @param events the events
     */
    public final void setEvents(final Map<String, EventDataObject> events) {
        this.events.clear();
        this.events.putAll(events);
    }

    /**
     * Gets operations.
     *
     * @return the operations
     */
    @JsonProperty
    public final Map<String, OperationDataObject> getOperations() {
        return operations;
    }

    /**
     * Sets operations.
     *
     * @param operations the operations
     */
    public final void setOperations(final Map<String, OperationDataObject> operations) {
        this.operations.clear();
        this.operations.putAll(operations);
    }

    private static <F extends FeatureConfiguration> void export(final ManagedResourceTemplate configuration,
                                                                final Map<String, ? extends AbstractFeatureDataObject<F>> source,
                                                                final Class<F> featureType){
        final EntityMap<? extends F> features = configuration.getFeatures(featureType);
        source.forEach((name, featureObject) -> featureObject.exportTo(features.getOrAdd(name)));
    }

    @Override
    public void exportTo(final E entity) {
        super.exportTo(entity);
        export(entity, attributes, AttributeConfiguration.class);
        export(entity, events, EventConfiguration.class);
        export(entity, operations, OperationConfiguration.class);
    }
}
