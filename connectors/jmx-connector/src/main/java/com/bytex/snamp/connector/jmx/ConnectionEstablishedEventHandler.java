package com.bytex.snamp.connector.jmx;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import java.io.IOException;

/**
 * Represents an event handler invoked when JMX connection is established.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
@FunctionalInterface
interface ConnectionEstablishedEventHandler extends MBeanServerConnectionHandler<Void> {
    void connectionEstablished(final MBeanServerConnection connection) throws IOException, JMException;

    @Override
    default Void handle(final MBeanServerConnection connection) throws IOException, JMException{
        connectionEstablished(connection);
        return null;
    }
}
