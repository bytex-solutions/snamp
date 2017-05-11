package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.health.ConnectionProblem;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ActuatorConnectorTest extends Assert {
    @Test
    public void connectionProblemTests() throws Exception {  //this test is run without any launched Spring application
        try(final ActuatorConnector connector = new ActuatorConnector("dummy", new URI("http://localhost:9233"), ManagedResourceConnector.EMPTY_CONFIGURATION)){
            assertTrue(connector.getStatus() instanceof ConnectionProblem);
        }
    }
}
