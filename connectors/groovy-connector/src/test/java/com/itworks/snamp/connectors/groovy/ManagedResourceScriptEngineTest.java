package com.itworks.snamp.connectors.groovy;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import static com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngineTest extends Assert {
    private final ManagedResourceScriptEngine engine;

    public ManagedResourceScriptEngineTest() throws IOException {
        engine = new ManagedResourceScriptEngine(getClass().getClassLoader(), "sample-groovy-scripts/");
    }

    @Test
    public void dummyAttributeTest() throws Exception {
        final SerializableAttributeConfiguration config = new SerializableAttributeConfiguration("DummyAttribute");
        config.setParameter("configParam", "Hello, world!");
        config.setReadWriteTimeout(TimeSpan.fromSeconds(2));

        final AttributeAccessor scr = engine.loadAttribute(new AttributeDescriptor(config));
        assertEquals(ManagedResourceAttributeScript.INT32, scr.type());
        assertTrue(scr.specifier().canRead());
        assertTrue(scr.specifier().canWrite());

        scr.setValue(20);
        assertEquals(20, scr.getValue());
    }
}
