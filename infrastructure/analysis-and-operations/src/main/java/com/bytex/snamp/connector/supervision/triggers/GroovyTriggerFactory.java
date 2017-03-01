package com.bytex.snamp.connector.supervision.triggers;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.util.Properties;

/**
 * Represents factory of Groovy-based triggers.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyTriggerFactory extends OSGiGroovyScriptEngine<GroovyTrigger> {
    GroovyTriggerFactory(final ClassLoader rootClassLoader) throws IOException {
        super(rootClassLoader, new Properties(), GroovyTrigger.class);
    }

    public GroovyTrigger create(final String text) {
        return parseScript(text, getGlobalVariables());
    }
}
