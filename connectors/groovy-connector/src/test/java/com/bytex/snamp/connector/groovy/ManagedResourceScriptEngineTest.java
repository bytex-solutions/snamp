package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.internal.OperatingSystem;
import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;

import static com.bytex.snamp.configuration.impl.SerializableAgentConfiguration.newEntityConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
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
        final AttributeConfiguration config = newEntityConfiguration(AttributeConfiguration.class);
        assertNotNull(config);
        config.getParameters().put("configParam", "Hello, world!");
        config.setReadWriteTimeout(2, ChronoUnit.SECONDS);

        final AttributeAccessor scr = engine.loadAttribute("DummyAttribute", new AttributeDescriptor(config));
        assertEquals(ManagedResourceAttributeScript.INT32, scr.type());
        assertTrue(scr.specifier().canRead());
        assertTrue(scr.specifier().canWrite());

        scr.setValue(20);
        assertEquals(20, scr.getValue());
    }
}
