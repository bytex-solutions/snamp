package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.*;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
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

    TemplateDataObject(final E configuration) {
        super(configuration);
        attributes = importFeatures(configuration, AttributeConfiguration.class, AttributeDataObject::new);
        events = importFeatures(configuration, EventConfiguration.class, EventDataObject::new);
        operations = importFeatures(configuration, OperationConfiguration.class, OperationDataObject::new);
    }

    private static <F extends FeatureConfiguration, DTO extends AbstractDataObject<F>> Map<String, DTO> importFeatures(final ManagedResourceTemplate template,
                                                                                                                       final Class<F> featureType,
                                                                                                                       final Function<? super F, DTO> dataObjectFactory) {
        return Exportable.importEntities(template.getFeatures(featureType), dataObjectFactory);
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

    private static <F extends FeatureConfiguration> void exportFeatures(final Map<String, ? extends AbstractFeatureDataObject<F>> source,
                                                                        final ManagedResourceTemplate destination,
                                                                        final Class<F> featureType){
        Exportable.exportEntities(source, destination.getFeatures(featureType));
    }

    @Override
    public void exportTo(@Nonnull final E entity) {
        super.exportTo(entity);
        exportFeatures(attributes, entity, AttributeConfiguration.class);
        exportFeatures(events, entity, EventConfiguration.class);
        exportFeatures(operations, entity, OperationConfiguration.class);
    }
}
