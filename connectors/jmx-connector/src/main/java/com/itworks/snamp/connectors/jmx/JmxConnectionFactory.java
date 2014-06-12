package com.itworks.snamp.connectors.jmx;

import javax.management.remote.JMXConnector;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface JmxConnectionFactory {
    JMXConnector createConnection() throws IOException;
}
