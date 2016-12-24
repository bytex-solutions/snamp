package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;
import com.google.common.collect.ObjectArrays;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import static com.bytex.snamp.MapUtils.toProperties;

/**
 * Represents loader of notification parsers written in Groovy language.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyNotificationParserLoader extends OSGiGroovyScriptEngine<GroovyNotificationParser> {

    //constructor for tests
    GroovyNotificationParserLoader(final ClassLoader classLoader,
                                   final URL... paths) throws IOException {
        super(classLoader, new Properties(), GroovyNotificationParser.class, paths);
    }

    public GroovyNotificationParserLoader(final MessageDrivenConnector connector,
                                          final Map<String, String> connectionParams,
                                          final boolean includeClassLoaderResources,
                                          final URL... paths) throws IOException {
        super(connector.getClass().getClassLoader(),
                toProperties(connectionParams),
                GroovyNotificationParser.class,
                includeClassLoaderResources ? ObjectArrays.concat(paths, connector.getClass().getResource("")) : paths);
    }

    public GroovyNotificationParserLoader(final MessageDrivenConnector connector,
                                          final Map<String, String> connectionParams) throws IOException {
        this(connector, connectionParams, true);
    }
}
