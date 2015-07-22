package com.itworks.snamp.adapters.ssh;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.jmx.json.Formatters;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshHelpers {
    static final TypeToken<Map<String, Object>> STRING_MAP_TYPE = new TypeToken<Map<String, Object>>() {};
    static final String ADAPTER_NAME = "ssh";
    private static final String LOGGER_NAME = AbstractResourceAdapter.getLoggerName(ADAPTER_NAME);
    static final Gson FORMATTER = Formatters.enableAll(new GsonBuilder())
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private SshHelpers(){

    }

    static <E extends Exception> void withLogger(final Consumer<Logger, E> contextBody) throws E {
        OSGiLoggingContext.within(LOGGER_NAME, contextBody);
    }

    private static void log(final Level lvl, final String message, final Object[] args, final Throwable e){
        withLogger(new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.log(lvl, String.format(message, args), e);
            }
        });
    }

    static void log(final Level lvl, final String message, final Throwable e){
        log(lvl, message, new Object[0], e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Throwable e){
        log(lvl, message, new Object[]{arg0}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Object arg2, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1, arg2}, e);
    }
}
