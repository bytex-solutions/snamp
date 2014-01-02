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
 * Represents tests for {@link ManagementConnectorBean} class.
 * @author Roman Sakno
 */
public final class IbmWmqConnectorTest extends SnampClassTestSet<IbmWmqConnector> {

    @Test
    public final void testConnectorBean() throws IntrospectionException, TimeoutException {
        final Map<String, String> env = new HashMap<String, String>()
        {{
            //put("queueFilter", "SYSTEM.*");
            //put("channelFilter", "SYSTEM.*");
            //put("serviceFilter", "SYSTEM.*");
        }};
        final IbmWmqConnector connector = new IbmWmqConnectorFactory().newInstance("wmq://SYSTEM.BKR.CONFIG@anticitizen.dhis.org:8000/TEST_QMGR", env);
        while(true)
            try {
                connector.connectAttribute("0", "servicesStatus", new HashMap<String, String>());
                connector.connectAttribute("1", "qmgrStatus", new HashMap<String, String>());
                connector.connectAttribute("2", "channelsStatus", new HashMap<String, String>());
                break;
            } catch (IllegalStateException e) { Thread.yield(); }  // ждем пока коннектор инициализируется

        final AttributeMetadata md = connector.getAttributeInfo("0");
        assertEquals("servicesStatus", md.getName());
        assert connector.getAttributeValue(md, TimeSpan.autoScale(10, TimeUnit.SECONDS), "") instanceof IbmWmqTypeSystem.ServiceStatusTable;
    }
}
