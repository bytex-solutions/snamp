package com.bytex.snamp.connector.md.groovy;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static com.bytex.snamp.MapUtils.toProperties;

/**
 * Represents loader of notification parsers written in Groovy language.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GroovyNotificationParserLoader extends OSGiGroovyScriptEngine<GroovyNotificationParser> {
    private final Logger logger;

    //constructor for tests
    GroovyNotificationParserLoader(final ClassLoader classLoader,
                                   final URL... paths) throws IOException {
        super(classLoader, new Properties(), GroovyNotificationParser.class, paths);
        logger = Logger.getAnonymousLogger();
    }

    public GroovyNotificationParserLoader(final MessageDrivenConnector connector,
                                          final Map<String, String> connectionParams,
                                          final URL... paths) throws IOException {
        super(connector.getClass().getClassLoader(), toProperties(connectionParams), GroovyNotificationParser.class, paths);
        logger = connector.getLogger();
    }

    public GroovyNotificationParserLoader(final MessageDrivenConnector connector,
                                          final Map<String, String> connectionParams) throws IOException {
        this(connector, connectionParams, connector.getClass().getResource(""));
    }

    @Override
    protected void interceptCreate(final GroovyNotificationParser script) {
        script.setLogger(logger);
    }
}
