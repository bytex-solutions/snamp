package com.itworks.snamp.testing.connectors.groovy;

import com.itworks.snamp.connectors.ManagedResourceConnector;
import org.junit.Test;

import javax.management.Attribute;
import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class GroovyConnectorTest extends AbstractGroovyConnectorTest {

    @Test
    public void dummyAttributeTest() throws JMException{
        final ManagedResourceConnector groovyConnector = getManagementConnector();
        try {
            groovyConnector.setAttribute(new Attribute("dummy", 42));
            final Object value = groovyConnector.getAttribute("dummy");
            assertTrue(value instanceof Integer);
            assertEquals(42, value);
        }
        finally {
            releaseManagementConnector();
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return true;
    }
}
