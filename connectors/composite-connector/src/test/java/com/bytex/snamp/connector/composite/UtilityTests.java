package com.bytex.snamp.connector.composite;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class UtilityTests extends Assert {
    @Test
    public void parseConnectionString(){
        final Map<String, String> strings = CompositeResourceConnector.parseConnectionString("jmx:= service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root; snmp:=192.168.0.1", ";");
        assertEquals(2, strings.size());
        assertEquals("service:jmx:rmi:///jndi/rmi://localhost:5657/karaf-root", strings.get("jmx"));
        assertEquals("192.168.0.1", strings.get("snmp"));
    }

    @Test
    public void parseParameters(){
        final Map<String, String> parameters = ImmutableMap.of(
            "param1", "value1",
            "jmx:param2", "value2",
            "jmx:param3", "value3",
            "snmp:param4", "value3"
        );
        final Function<String, Map<String, String>> parsedParams = ConnectionParametersMap.parse(parameters);
        final Map<String, String> jmxParams = parsedParams.apply("jmx");
        assertEquals(2, jmxParams.size());
        assertEquals("value2", jmxParams.get("param2"));
        assertEquals("value3", jmxParams.get("param3"));
        assertTrue(parsedParams.apply("ssh").isEmpty());
    }
}
