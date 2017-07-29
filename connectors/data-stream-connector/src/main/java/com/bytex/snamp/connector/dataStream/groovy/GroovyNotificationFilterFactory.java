package com.bytex.snamp.connector.dataStream.groovy;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GroovyNotificationFilterFactory extends OSGiGroovyScriptEngine<GroovyNotificationFilter> {
    public GroovyNotificationFilterFactory(final ClassLoader rootClassLoader) {
        super(rootClassLoader, new Properties(), GroovyNotificationFilter.class);
    }

    public GroovyNotificationFilter create(final String text){
        return parseScript(text, getGlobalVariables());
    }
}
