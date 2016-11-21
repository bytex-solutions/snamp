package com.bytex.snamp.webconsole.model.dto;


import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
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

}
