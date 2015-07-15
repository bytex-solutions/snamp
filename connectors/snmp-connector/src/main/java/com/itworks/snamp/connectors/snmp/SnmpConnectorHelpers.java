package com.itworks.snamp.connectors.snmp;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.AbstractManagedResourceConnector;
import com.itworks.snamp.core.OSGiLoggingContext;
import org.snmp4j.smi.OID;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpConnectorHelpers {
    private static final String DISCOVERY_TIMEOUT_PROPERTY = "com.itworks.snamp.connectors.snmp.discoveryTimeout";
    static final String CONNECTOR_NAME = "snmp";
    private static final String LOGGER_NAME = AbstractManagedResourceConnector.getLoggerName(CONNECTOR_NAME);

    private SnmpConnectorHelpers(){

    }

    private static int[] getPostfix(final int[] prefix, final int[] full){
        return full.length > prefix.length ?
                Arrays.copyOfRange(full, prefix.length, full.length):
                new int[0];
    }

    public static OID getPostfix(final OID prefix, final OID oid){
        return oid.startsWith(prefix) ? new OID(getPostfix(prefix.getValue(), oid.getValue())) : new OID();
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

    static void log(final Level lvl, final String message){
        log(lvl, message, null);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Throwable e){
        log(lvl, message, new Object[]{arg0}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1}, e);
    }

    static void log(final Level lvl, final String message, final Object arg0, final Object arg1, final Object arg2, final Throwable e){
        log(lvl, message, new Object[]{arg0, arg1, arg2}, e);
    }

    static <T> ArrayType<T[]> arrayType(final SimpleType<T> type,
                                                final boolean primitive) throws ExceptionInInitializerError{
        try {
            return new ArrayType<>(type, primitive);
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static TimeSpan getDiscoveryTimeout(){
        return new TimeSpan(Long.parseLong(System.getProperty(DISCOVERY_TIMEOUT_PROPERTY, "5000")));
    }
}
