package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Represents script engine for Gateway scripts.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GatewayScriptEngine extends OSGiGroovyScriptEngine<GatewayScript> {
    public GatewayScriptEngine(final ClassLoader rootClassLoader,
                               final Properties properties,
                               final URL... paths) throws IOException {
        super(rootClassLoader, properties, GatewayScript.class, paths);
    }

    public GatewayScript createScript(final String scriptFile,
                                      final Map<String, ?> environment) throws ResourceException, ScriptException {
        return createScript(scriptFile, new Binding(environment));
    }

    @Override
    protected void interceptCreate(final GatewayScript script) {
        script.setBundleContext(getBundleContext());
    }
}
