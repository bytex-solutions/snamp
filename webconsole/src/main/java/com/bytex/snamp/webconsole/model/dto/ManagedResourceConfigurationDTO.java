package com.bytex.snamp.webconsole.model.dto;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.OperationConfiguration;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ManagedResourceConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class ManagedResourceConfigurationDTO extends AbstractDTOClass<ManagedResourceConfiguration>{

    private Map<String, AttributeDTOEntity> attributes;
    private Map<String, EventDTOEntity> events;
    private Map<String, OperationDTOEntity> operations;
    private String connectionString;
    private String type;
    private Map<String, String> parameters;


    /**
     * Gets attributes.
     *
     * @return the attributes
     */
    public Map<String, AttributeDTOEntity> getAttributes() {
        return attributes;
    }

    /**
     * Sets attributes.
     *
     * @param attributes the attributes
     */
    public void setAttributes(Map<String, AttributeDTOEntity> attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets events.
     *
     * @return the events
     */
    public Map<String, EventDTOEntity> getEvents() {
        return events;
    }

    /**
     * Sets events.
     *
     * @param events the events
     */
    public void setEvents(Map<String, EventDTOEntity> events) {
        this.events = events;
    }

    /**
     * Gets operations.
     *
     * @return the operations
     */
    public Map<String, OperationDTOEntity> getOperations() {
        return operations;
    }

    /**
     * Sets operations.
     *
     * @param operations the operations
     */
    public void setOperations(Map<String, OperationDTOEntity> operations) {
        this.operations = operations;
    }

    /**
     * Gets connection string.
     *
     * @return the connection string
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Sets connection string.
     *
     * @param connectionString the connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets parameters.
     *
     * @param parameters the parameters
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public ManagedResourceConfigurationDTO build(ManagedResourceConfiguration object) {
        if (object != null) {
            this.setConnectionString(object.getConnectionString());
            this.setType(object.getType());
            this.setAttributes(object.getFeatures(AttributeConfiguration.class).entrySet()
                    .stream().collect( Collectors.toMap(Map.Entry::getKey,
                            entry -> new AttributeDTOEntity(entry.getValue().getParameters(),
                                    entry.getValue().getReadWriteTimeout()))));

            this.setEvents(object.getFeatures(EventConfiguration.class).entrySet()
                    .stream().collect( Collectors.toMap(Map.Entry::getKey,
                            entry -> new EventDTOEntity(entry.getValue().getParameters()))));

            this.setOperations(object.getFeatures(OperationConfiguration.class).entrySet()
                    .stream().collect( Collectors.toMap(Map.Entry::getKey,
                            entry -> new OperationDTOEntity(entry.getValue().getParameters(),
                                    entry.getValue().getInvocationTimeout()))));

            this.setParameters(object.getParameters());
        }
        return this;
    }
}
