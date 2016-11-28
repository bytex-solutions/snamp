package com.bytex.snamp.connector.composite;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Represents script engine for aggregation attribute.
 */
final class ScriptLoader extends OSGiGroovyScriptEngine<AggregationAttributeScriptlet> {
    private final Logger logger;

    ScriptLoader(final ClassLoader rootClassLoader, final Logger logger, final URL... paths) throws IOException {
        super(rootClassLoader, new Properties(), AggregationAttributeScriptlet.class, paths);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    protected void interceptCreate(final AggregationAttributeScriptlet script) {
        script.setLogger(logger);
        script.setBundleContext(getBundleContext());
    }
}
