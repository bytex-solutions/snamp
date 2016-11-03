package com.bytex.snamp.connector.composite;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;
import com.bytex.snamp.scripting.groovy.Scriptlet;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Represents script engine for aggregation attribute.
 */
final class ScriptLoader extends OSGiGroovyScriptEngine<AggregationAttributeScriptlet> {
    ScriptLoader(final ClassLoader rootClassLoader, final Logger logger, final String... paths) throws IOException {
        super(rootClassLoader, new Properties(), AggregationAttributeScriptlet.class, paths);
        Scriptlet.setLogger(getGlobalVariables(), logger);
    }
}
