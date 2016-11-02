package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;
import com.bytex.snamp.scripting.groovy.ScriptingAPISupport;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Represents script engine for Gateway scripts.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class GatewayScriptEngine extends OSGiGroovyScriptEngine<GatewayScript> {
    public GatewayScriptEngine(final ClassLoader rootClassLoader,
                               final Logger logger,
                               final Properties properties,
                               final String... paths) throws IOException {
        super(rootClassLoader, properties, GatewayScript.class, paths);
        ScriptingAPISupport.setLogger(getGlobalVariables(), logger);
    }

    public GatewayScript createScript(final String scriptFile,
                                      final Map<String, ?> environment) throws ResourceException, ScriptException {
        return createScript(scriptFile, new Binding(environment));
    }
}
