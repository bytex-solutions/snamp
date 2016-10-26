package com.bytex.snamp.connector.jmx;

import javax.management.remote.JMXConnector;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
interface JmxConnectionFactory {
    JMXConnector createConnection() throws IOException;
}
