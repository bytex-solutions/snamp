package com.bytex.snamp.supervision.health.triggers;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.util.Properties;

/**
 * Represents factory of Groovy-based triggers.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class GroovyTriggerFactory extends OSGiGroovyScriptEngine<GroovyTrigger> {
    GroovyTriggerFactory(final ClassLoader rootClassLoader) {
        super(rootClassLoader, new Properties(), GroovyTrigger.class);
    }

    @Override
    protected void interceptCreate(final GroovyTrigger script) {
        script.setBundleContext(getBundleContext());
    }

    public GroovyTrigger create(final String text) {
        return parseScript(text, getGlobalVariables());
    }
}
