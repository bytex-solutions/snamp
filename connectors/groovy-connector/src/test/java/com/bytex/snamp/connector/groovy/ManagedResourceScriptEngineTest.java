package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngineTest extends Assert {
    private final ManagedResourceScriptEngine engine;

    public ManagedResourceScriptEngineTest() throws IOException, URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource("scripts/");
        assertNotNull(resource);
        engine = new ManagedResourceScriptEngine("testResource", Logger.getLogger("test"), getClass().getClassLoader(), false, new Properties(), resource);
    }

    @Test
    public void dummyAttributeTest() throws Exception {
        final ManagedResourceScriptlet scriptlet = engine.createScript("DummyAttribute.groovy", null);
        scriptlet.run();
        final GroovyAttribute attribute = scriptlet.createAttribute("CustomAttribute", new AttributeDescriptor(null, ImmutableMap.of()));
        attribute.setValue(42L);
        assertEquals(42L, attribute.getValue());
    }
}
