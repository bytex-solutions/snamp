package com.itworks.snamp.management.impl;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.core.OSGiLoggingContext;

import javax.management.openmbean.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Monitoring utils.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class MonitoringUtils {
    private static final String LOGGER_NAME = "com.itworks.snamp.management.impl";

    private MonitoringUtils(){

    }

    /**
     * With logger.
     *
     * @param <E>  the type parameter
     * @param loggerHandler the logger handler
     * @throws E the e
     */
    static <E extends Exception> void withLogger(final Consumer<Logger, E> loggerHandler) throws E{
        OSGiLoggingContext.within(LOGGER_NAME, loggerHandler);
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e){
        withLogger(new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.log(lvl, String.format(message, args), e);
            }
        });
    }

    /**
     * Log void.
     *
     * @param lvl the lvl
     * @param message the message
     * @param e the e
     */
    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, new Object[0], e);
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
