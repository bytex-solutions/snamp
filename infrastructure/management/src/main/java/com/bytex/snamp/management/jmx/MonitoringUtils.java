package com.bytex.snamp.management.jmx;

import com.google.common.collect.ImmutableMap;

import javax.management.openmbean.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The type Monitoring utils.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MonitoringUtils {
    private static final String LOGGER_NAME = "com.bytex.snamp.management.impl";

    private MonitoringUtils(){

    }

    /**
     * Gets logger.
     *
     * @return the logger
     */
    static Logger getLogger() {
        return Logger.getLogger(LOGGER_NAME);
    }

    /**
     * Transform tabular data to map.
     *
     * @param data the data
     * @return the map
     */
    public static Map<String, String> transformTabularDataToMap(final TabularData data) {
        if (data == null || data.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<String, String> result = new HashMap<>();
            for (Object value : data.values()) {
                if (!(value instanceof CompositeData)) continue;
                final CompositeData cd = (CompositeData) value;
                result.put((String) cd.get("Key"), (String) cd.get("Value"));
            }
            return result;
        }
    }

    /**
     * Transform additional properties to tabular data.
     *
     * @param map the map
     * @return the tabular data support
     * @throws OpenDataException the open data exception
     */
    public static TabularDataSupport transformAdditionalPropertiesToTabularData(final Map<String, String> map) throws OpenDataException {
        final TabularDataSupport tabularDataSupport = new TabularDataSupport(CommonOpenTypesSupport.SIMPLE_MAP_TYPE);
        if (map != null) {
            for (final Map.Entry<String, String> entry : map.entrySet()) {
                tabularDataSupport.put(new CompositeDataSupport(tabularDataSupport.getTabularType().getRowType(),
                        ImmutableMap.<String, Object>of(
                                "Key", entry.getKey(),
                                "Value", entry.getValue())));
            }
        }
        return tabularDataSupport;
    }
}
