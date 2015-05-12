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
public final class ManagementScriptEngineTest extends Assert {
    private final ManagementScriptEngine engine;

    public ManagementScriptEngineTest() throws IOException {
        engine = new ManagementScriptEngine("sample-groovy-scripts/");
    }

    @Test
    public void dummyAttributeTest() throws Exception {
        final SerializableAttributeConfiguration config = new SerializableAttributeConfiguration("DummyAttribute");
        config.setParameter("configParam", "Hello, world!");
        config.setReadWriteTimeout(TimeSpan.fromSeconds(2));

        final AttributeScript scr = engine.loadAttribute(new AttributeDescriptor(config));
        scr.run();
        assertEquals(AttributeScript.INT32, scr.type());
        assertTrue(scr.isReadable());
        assertTrue(scr.isWritable());

        scr.setValue(20);
        assertEquals(20, scr.getValue());
    }
}
