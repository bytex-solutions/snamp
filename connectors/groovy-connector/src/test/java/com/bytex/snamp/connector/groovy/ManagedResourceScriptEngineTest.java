package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class ManagedResourceScriptEngineTest extends Assert {
    private final ManagedResourceScriptEngine engine;

    public ManagedResourceScriptEngineTest() throws IOException, URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource("scripts/");
        assertNotNull(resource);
        final String path = ManagedResourceScriptEngine.createPath("ResourceScript.groovy", resource.toString());
        engine = new ManagedResourceScriptEngine("testResource", getClass().getClassLoader(), false, new Properties(), path);
    }

    @Test
    public void dummyAttributeTest() throws Exception {
        final ManagedResourceScriptlet scriptlet = engine.createScript("ResourceScript.groovy", null);
        scriptlet.run();
        final GroovyAttribute attribute = scriptlet.createAttribute("CustomAttribute", new AttributeDescriptor(null, ImmutableMap.of()));
        attribute.setValue(42L);
        assertEquals(42L, attribute.getValue());
    }

    @Test
    public void operationTest() throws Exception {
        final ManagedResourceScriptlet scriptlet = engine.createScript("ResourceScript.groovy", null);
        scriptlet.run();
        final GroovyOperation operation = scriptlet.createOperation("CustomOperation", new OperationDescriptor(null, ImmutableMap.of()));
        assertEquals(100D, operation.invoke(10D, 90D));
    }
}
