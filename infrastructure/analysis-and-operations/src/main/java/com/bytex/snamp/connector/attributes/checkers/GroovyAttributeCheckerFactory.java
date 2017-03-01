package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.util.Properties;

/**
 * Represents factory of {@link GroovyAttributeChecker}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class GroovyAttributeCheckerFactory extends OSGiGroovyScriptEngine<GroovyAttributeChecker> {
    GroovyAttributeCheckerFactory(final ClassLoader rootClassLoader) throws IOException {
        super(rootClassLoader, new Properties(), GroovyAttributeChecker.class);
    }

    GroovyAttributeChecker create(final String text) {
        return parseScript(text, getGlobalVariables());
    }
}
