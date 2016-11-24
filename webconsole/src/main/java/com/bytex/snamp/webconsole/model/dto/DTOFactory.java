package com.bytex.snamp.webconsole.model.dto;

import com.bytex.snamp.configuration.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Dto factory.
 */
public class DTOFactory {

    private DTOFactory() {}

    /**
     * Build abstract dto class.
     *
     * @param source the source
     * @return the abstract dto class
     */
    public static ManagedResourceConfigurationDTO buildManagedResource(final ManagedResourceConfiguration source) {
        return new ManagedResourceConfigurationDTO(source);
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static Map buildManagedResources(final EntityMap<? extends ManagedResourceConfiguration> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new ManagedResourceConfigurationDTO(e.getValue())));
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static Map buildAttributes(final EntityMap<? extends AttributeConfiguration> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new AttributeDTOEntity(e.getValue().getParameters(), e.getValue().getReadWriteTimeout())));
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static Map buildEvents(final EntityMap<? extends EventConfiguration> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new EventDTOEntity(e.getValue().getParameters())));
    }


    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static Map buildOperations(final EntityMap<? extends OperationConfiguration> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new OperationDTOEntity(e.getValue().getParameters(), e.getValue().getInvocationTimeout())));
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static AttributeDTOEntity buildAttribute(final AttributeConfiguration source) {
        return new AttributeDTOEntity(source.getParameters(), source.getReadWriteTimeout());
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static EventDTOEntity buildEvent(final EventConfiguration source) {
        return new EventDTOEntity(source.getParameters());
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static OperationDTOEntity buildOperation(final OperationConfiguration source) {
        return new OperationDTOEntity(source.getParameters(), source.getInvocationTimeout());
    }

    /**
     * Build abstract dto class.
     *
     * @param source the source
     * @return the abstract dto class
     */
    public static GatewayConfigurationDTO buildGateway(final GatewayConfiguration source) {
        return new GatewayConfigurationDTO(source.getParameters(), source.getType());
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static Map buildGateways(final EntityMap<? extends GatewayConfiguration> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new GatewayConfigurationDTO(e.getValue().getParameters(), e.getValue().getType())));
    }
}
