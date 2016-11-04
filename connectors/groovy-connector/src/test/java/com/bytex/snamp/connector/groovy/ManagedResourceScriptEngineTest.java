package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
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
        final URL resource = getClass().getClassLoader().getResource("scripts/DummyAttribute.groovy");
        assertNotNull(resource);
        final String scriptPath = new File(resource.toURI()).getParent();
        engine = new ManagedResourceScriptEngine("testResource", Logger.getLogger("test"), getClass().getClassLoader(), new Properties(), scriptPath);
    }

    @Test
    public void dummyAttributeTest() throws Exception {
        final AttributeConfiguration config = newEntityConfiguration(AttributeConfiguration.class);
        assertNotNull(config);
        config.getParameters().put("configParam", "Hello, world!");
        config.setReadWriteTimeout(2, ChronoUnit.SECONDS);

        final AttributeAccessor scr = engine.loadAttribute("DummyAttribute", new AttributeDescriptor(config));
        assertEquals(ManagedResourceAttributeScriptlet.INT32, scr.type());
        assertTrue(scr.specifier().canRead());
        assertTrue(scr.specifier().canWrite());

        scr.setValue(20);
        assertEquals(20, scr.getValue());
    }
}
