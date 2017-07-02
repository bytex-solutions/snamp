package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.ManagedResourceTemplate;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

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
        attributes = Exportable.importEntities(configuration.getAttributes(), AttributeDataObject::new);
        events = Exportable.importEntities(configuration.getEvents(), EventDataObject::new);
        operations = Exportable.importEntities(configuration.getOperations(), OperationDataObject::new);
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

    @Override
    public void exportTo(@Nonnull final E entity) {
        super.exportTo(entity);
        Exportable.exportEntities(attributes, entity.getAttributes());
        Exportable.exportEntities(events, entity.getEvents());
        Exportable.exportEntities(operations, entity.getOperations());
    }
}
