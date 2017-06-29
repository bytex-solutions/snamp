package com.bytex.snamp.connector.actuator;

import com.bytex.snamp.connector.health.ConnectionProblem;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ActuatorConnectorTest extends Assert {
    @Test
    public void connectionProblemTests() throws Exception {  //this test is run without any launched Spring application
        try(final ActuatorConnector connector = new ActuatorConnector("dummy", new ActuatorConnectionOptions(new URI("http://localhost:9233"), Collections.emptyMap()))){
            assertTrue(connector.getStatus() instanceof ConnectionProblem);
        }
    }
}
