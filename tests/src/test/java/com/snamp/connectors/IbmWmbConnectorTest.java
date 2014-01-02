package com.snamp.connectors;

import com.snamp.SnampClassTestSet;
import com.snamp.TimeSpan;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
            //put("application", "TEST_APP");
            //put("messageFlow", "TEST_FLOW");
        }};
        final IbmWmbConnector connector = new IbmWmbConnectorFactory().newInstance("wmb://anticitizen.dhis.org:8000/TEST_QMGR", env);
        while(true)
            try {
                connector.connectAttribute("0", "name", new HashMap<String, String>());
                connector.connectAttribute("1", "runningChildrenNames", new HashMap<String, String>());
                connector.connectAttribute("2", "properties", new HashMap<String, String>());
                break;
            } catch (IllegalStateException e) { Thread.yield(); }  // ждем пока коннектор инициализируется

        final AttributeMetadata md = connector.getAttributeInfo("0");
        assertEquals("name", md.getName());
        assertEquals(connector.getAttributeValue(md, TimeSpan.autoScale(10, TimeUnit.SECONDS), ""), "TEST");
    }
}
