package com.itworks.snamp.adapters.http;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.WellKnownType;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.Servlet3Continuation;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAdapterHelpers {
    static final String ADAPTER_NAME = "http";
    private static final String LOGGER_NAME = AbstractResourceAdapter.getLoggerName(ADAPTER_NAME);

    private HttpAdapterHelpers(){

    }

    //do not remove. It is necessary for Atmosphere and maven-bundle-plugin for correct import of Jetty package
    @SpecialUse
    private static Class<? extends Continuation> getJettyContinuationClass(){
        return Servlet3Continuation.class;
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

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1}, e);
    }

    static String getJsonType(final WellKnownType type){
        switch (type){
            case STRING:
            case CHAR: return "String";
            case BIG_INT:
            case BIG_DECIMAL:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE: return "Number";
            case BOOL: return "Boolean";
            default:
                if(type.isArray() || type.isBuffer()) return "Array";
            case DICTIONARY:
            case TABLE:
                return "Object";
        }
    }
}
