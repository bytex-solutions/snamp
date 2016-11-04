package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.util.Properties;

/**
 * Represents loader of notification parsers written in Groovy language.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyNotificationLoader extends OSGiGroovyScriptEngine<GroovyNotificationParser> {
    public GroovyNotificationLoader(final ClassLoader rootClassLoader,
                                    final Properties properties,
                                    final String... paths) throws IOException {
        super(rootClassLoader, properties, GroovyNotificationParser.class, paths);
    }
}
