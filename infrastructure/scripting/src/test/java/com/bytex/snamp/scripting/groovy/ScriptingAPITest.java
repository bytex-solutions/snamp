package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.concurrent.Repeater;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class ScriptingAPITest extends Assert {
    private final OSGiGroovyScriptEngine<Scriptlet> engine;

    public ScriptingAPITest() throws IOException, URISyntaxException {
        final URL resource = getClass().getClassLoader().getResource("scripts/");
        assertNotNull(resource);
        engine = new OSGiGroovyScriptEngine<Scriptlet>(getClass().getClassLoader(), new Properties(), Scriptlet.class, resource){
            @Override
            protected void interceptCreate(final Scriptlet script) {
                script.setBundleContext(getBundleContext());
            }
        };
    }

    @Test
    public void callScript() throws ResourceException, ScriptException {
        final Object result = engine.run("ApiTestScript.groovy", new Binding());
        assertTrue(result instanceof Repeater);
    }
}
