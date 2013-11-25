package com.snamp.connectors;

import com.snamp.SnampClassTestSet;
import com.snamp.TimeSpan;
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
            put("host", "10.200.100.113");
            put("port", "1450");
            put("qmgr", "OEC_QMGR");
        }};
        final IbmWmbConnector connector = new IbmWmbConnectorFactory().newInstance("", env);
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
