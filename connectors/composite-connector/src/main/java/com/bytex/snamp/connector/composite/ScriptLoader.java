package com.bytex.snamp.connector.composite;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Represents script engine for aggregation attribute.
 */
final class ScriptLoader extends OSGiGroovyScriptEngine<AggregationAttributeScriptlet> {

    ScriptLoader(final ClassLoader rootClassLoader, final URL... paths) throws IOException {
        super(rootClassLoader, new Properties(), AggregationAttributeScriptlet.class, paths);
    }

    @Override
    protected void interceptCreate(final AggregationAttributeScriptlet script) {
        script.setBundleContext(getBundleContext());
    }
}
