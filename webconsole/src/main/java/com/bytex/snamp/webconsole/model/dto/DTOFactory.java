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
    public static AbstractDTOClass build(final ManagedResourceConfiguration source) {
        return new ManagedResourceConfigurationDTO().build(source);
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static Map build(final EntityMap<? extends ManagedResourceConfiguration> source) {
        return source.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new ManagedResourceConfigurationDTO().build(e.getValue())));
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
    public static AttributeDTOEntity build(final AttributeConfiguration source) {
        return new AttributeDTOEntity(source.getParameters(), source.getReadWriteTimeout());
    }

    /**
     * Build map.
     *
     * @param source the source
     * @return the map
     */
    public static EventDTOEntity build(final EventConfiguration source) {
        return new EventDTOEntity(source.getParameters());
    }
}
