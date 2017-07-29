package org.snmp4j.log;

import java.util.logging.Logger;

/**
 * Represents log bridge between OSGi logging subsystem
 * and SNMP4J logging subsystem.
 * This class cannot be inherited or instantiated directly.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class OSGiLogFactory extends JavaLogFactory {
    private final Logger rootLogger;

    private OSGiLogFactory(final String loggerName){
        rootLogger = Logger.getLogger(loggerName);
    }

    /**
     * Setups this logger as a default logging subsystem for SNMP4J.
     */
    public static boolean setup() {
        return setup("org.snmp4j");
    }

    public static synchronized boolean setup(final String loggerName){
        if (getLogFactory() instanceof OSGiLogFactory)
            return false;
        else {
            setLogFactory(new OSGiLogFactory(loggerName));
            return true;
        }
    }

    @Override
    public JavaLogAdapter getRootLogger() {
        return new JavaLogAdapter(rootLogger);
    }
}
