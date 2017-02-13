package com.bytex.snamp.connector.dataStream.groovy;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyNotificationFilterFactory extends OSGiGroovyScriptEngine<GroovyNotificationFilter> {
    public GroovyNotificationFilterFactory(final ClassLoader rootClassLoader) throws IOException {
        super(rootClassLoader, new Properties(), GroovyNotificationFilter.class);
    }

    public GroovyNotificationFilter create(final String text){
        return parseScript(text, getGlobalVariables());
    }
}
