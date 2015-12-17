package com.bytex.snamp.connectors.groovy;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.internal.OperatingSystem;
import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngineTest extends Assert {
    private final ManagedResourceScriptEngine engine;

    private static String getScriptDir(){
        String path = System.getProperty("DummyScriptFile");
        if(!Strings.isNullOrEmpty(path))
            path = Paths.get(path, OperatingSystem.isWindows() ? "sample-groovy-scripts\\" : "sample-groovy-scripts/").toAbsolutePath().toString();
        return path;
    }

    public ManagedResourceScriptEngineTest() throws IOException {
        engine = new ManagedResourceScriptEngine(getClass().getClassLoader(), getScriptDir());
    }

    @Test
    public void dummyAttributeTest() throws Exception {
        final SerializableAttributeConfiguration config = new SerializableAttributeConfiguration();
        config.setParameter("configParam", "Hello, world!");
        config.setReadWriteTimeout(TimeSpan.ofSeconds(2));

        final AttributeAccessor scr = engine.loadAttribute("DummyAttribute", new AttributeDescriptor(config));
        assertEquals(ManagedResourceAttributeScript.INT32, scr.type());
        assertTrue(scr.specifier().canRead());
        assertTrue(scr.specifier().canWrite());

        scr.setValue(20);
        assertEquals(20, scr.getValue());
    }
}
