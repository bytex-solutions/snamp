package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.internal.OperatingSystem;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ScriptingAPITest extends Assert {
    private final OSGiGroovyScriptEngine<ScriptingAPISupport> engine;

    public ScriptingAPITest() throws IOException {
        final Path path = Paths.get(System.getProperty("DummyScriptFile"), OperatingSystem.isWindows() ? "sample-groovy-scripts\\" : "sample-groovy-scripts/");
        engine = new OSGiGroovyScriptEngine<>(getClass().getClassLoader(),
                new Properties(),
                ScriptingAPISupport.class,
                path.toString());
        ScriptingAPISupport.setLogger(engine.getGlobalVariables(), Logger.getLogger("TestLogger"));
    }

    @Test
    public void callScript() throws ResourceException, ScriptException {
        final Object result = engine.run("ApiTestScript.groovy", new Binding());
        assertTrue(result instanceof Repeater);
    }
}
