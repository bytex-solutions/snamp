package com.bytex.snamp.connectors.jmx;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import java.io.IOException;
import java.util.EventListener;

/**
 * Represents Management Bean connection handler.
 * @param <T> Type of the connection handling result.
 */
@FunctionalInterface
interface MBeanServerConnectionHandler<T> extends EventListener {
    /**
     * Extracts object from the connection,
     *
     * @param connection The connection to process.
     * @return MBean connection processing result.
     * @throws java.io.IOException          Communication troubles.
     * @throws javax.management.JMException JMX exception caused on remote side.
     */
    T handle(final MBeanServerConnection connection) throws IOException, JMException;
}
