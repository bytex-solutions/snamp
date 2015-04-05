package org.snmp4j.log;

import java.util.Collections;
import java.util.Iterator;

/**
 * Represents log bridge between OSGi {@link org.osgi.service.log.LogService}
 * and SNMP4J logging subsystem.
 * This class cannot be inherited or instantiated directly.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OSGiLogFactory extends LogFactory {
    private static final OSGiLogAdapter ROOT_LOGGER = new OSGiLogAdapter("SNMP4J");

    private OSGiLogFactory(){
    }

    /**
     * Setups this logger as a default logging subsystem for SNMP4J.
     */
    public static synchronized boolean setup() {
        if (getLogFactory() instanceof OSGiLogFactory)
            return false;
        else {
            setLogFactory(new OSGiLogFactory());
            return true;
        }
    }

    /**
     * Creates a Logger for the specified class. This method returns the
     * {@link NoLogger} logger instance which disables logging.
     * Overwrite this method the return a custom logger to enable logging for
     * SNMP4J.
     *
     * @param c the class for which a logger needs to be created.
     * @return the <code>LogAdapter</code> instance.
     */
    @Override
    protected OSGiLogAdapter createLogger(final Class c) {
        return createLogger(c.getName());
    }

    /**
     * Creates a Logger for the specified class. This method returns the
     * {@link NoLogger} logger instance which disables logging.
     * Overwrite this method the return a custom logger to enable logging for
     * SNMP4J.
     *
     * @param className the class name for which a logger needs to be created.
     * @return the <code>LogAdapter</code> instance.
     * @since 1.7
     */
    @Override
    protected OSGiLogAdapter createLogger(final String className) {
        return new OSGiLogAdapter(className);
    }

    /**
     * Returns the top level logger.
     *
     * @return a LogAdapter instance.
     * @since 1.7
     */
    @Override
    public OSGiLogAdapter getRootLogger() {
        return ROOT_LOGGER;
    }

    /**
     * Returns all available LogAdapters in depth first order.
     *
     * @return a read-only Iterator.
     * @since 1.7
     */
    @Override
    public Iterator loggers() {
        return Collections.emptyIterator();
    }
}
