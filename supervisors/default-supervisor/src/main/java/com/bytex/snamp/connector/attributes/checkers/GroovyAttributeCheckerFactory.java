package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.util.Properties;

/**
 * Represents factory of {@link GroovyAttributeChecker}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class GroovyAttributeCheckerFactory extends OSGiGroovyScriptEngine<GroovyAttributeChecker> {
    GroovyAttributeCheckerFactory(final ClassLoader rootClassLoader) {
        super(rootClassLoader, new Properties(), GroovyAttributeChecker.class);
    }

    GroovyAttributeChecker create(final String text) {
        return parseScript(text, getGlobalVariables());
    }

    @Override
    protected void interceptCreate(final GroovyAttributeChecker script) {
        script.setBundleContext(getBundleContext());
    }
}
