package com.snamp.connectors;

import com.snamp.SnampClassTestSet;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Represents tests for {@link com.snamp.connectors.ManagementConnectorBean} class.
 * @author Roman Sakno
 */
public final class IbmWmbConnectorTest extends SnampClassTestSet<IbmWmbConnector> {

    @Test
    public final void testConnectorBean() throws IntrospectionException, TimeoutException {
        final Map<String, String> env = new HashMap<String, String>()
        {{
            put("executionGroup", "TEST");
            put("application", "TESTAPP");
        }};
        //final IbmWmbConnector connector = new IbmWmbConnectorFactory().newInstance("broker://10.200.100.113:1450/OEC_QMGR", env);
        final IbmWmbConnector connector = new IbmWmbConnectorFactory().newInstance("broker://192.168.0.69:1450/TEST_QMGR", env);
        connector.connectAttribute("0", "executionGroupCount", new HashMap<String, String>());
        //assertEquals(connector.getProperty1(), connector.getAttribute("0", TimeSpan.INFINITE, ""));
        //connector.setAttribute("0", TimeSpan.INFINITE, "1234567890");
        //assertEquals(connector.getProperty1(), connector.getAttribute("0", TimeSpan.INFINITE, ""));
        final AttributeMetadata md = connector.getAttributeInfo("0");
        //assertTrue(md.canRead());
        //assertTrue(md.canWrite());
        assertEquals("executionGroupCount", md.getAttributeName());
        //assertTrue(md.getAttributeType().canConvertFrom(String.class));
    }
}
