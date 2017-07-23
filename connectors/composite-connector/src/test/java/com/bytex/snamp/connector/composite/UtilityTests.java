package com.bytex.snamp.connector.composite;

import com.bytex.snamp.configuration.ManagedResourceInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class UtilityTests extends Assert {

    @Test
    public void parseConnectionString(){
        final ManagedResourceInfo source = ComposedConfiguration.createResourceInfo("jmx:= service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root; snmp:=192.168.0.1", "");
        final Map<String, ManagedResourceInfo> strings = ComposedConfiguration.parse(source, ";");
        assertEquals(2, strings.size());
        assertEquals("service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root", strings.get("jmx").getConnectionString());
        assertEquals("192.168.0.1", strings.get("snmp").getConnectionString());
    }

    @Test
    public void parseParameters() {
        final ManagedResourceInfo source = ComposedConfiguration.createResourceInfo("jmx:= service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root; snmp:=192.168.0.1", "");
        source.put("param1", "value1");
        source.put("jmx:param2", "value2");
        source.put("jmx:param3", "value3");
        source.put("snmp:param4", "value3");
        final Map<String, ManagedResourceInfo> parsedParams = ComposedConfiguration.parse(source, ";");
        final Map<String, String> jmxParams = parsedParams.get("jmx");
        assertEquals(2, jmxParams.size());
        assertEquals("value2", jmxParams.get("param2"));
        assertEquals("value3", jmxParams.get("param3"));
        assertNull(parsedParams.get("ssh"));
    }
}
