package com.bytex.snamp.connector.http;

import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.groovy.GroovyNotificationParser;
import com.bytex.snamp.connector.md.groovy.GroovyNotificationParserLoader;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import groovy.lang.Binding;

import java.net.URL;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents HTTP acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpAcceptor extends MessageDrivenConnector {

    HttpAcceptor(final String resourceName, final Map<String, String> parameters) {
        super(resourceName, parameters, HttpConnectorConfigurationDescriptor.getInstance());
    }

    @Override
    protected GroovyNotificationParser createNotificationParser(final String resourceName, final NotificationSource source, final Map<String, String> parameters) {
        return callUnchecked(() -> {
            final URL[] path = HttpConnectorConfigurationDescriptor.getInstance().parseScriptPath(parameters);
            //load standard HTTP parser for measurements
            GroovyNotificationParserLoader loader = new GroovyNotificationParserLoader(this, parameters, true, path);
            final GroovyNotificationParser mainParser = loader.createScript("MeasurementParser.groovy", new Binding());
            //load user-defined parser
            final String scriptFile = HttpConnectorConfigurationDescriptor.getInstance().parseScriptFile(parameters);
            if(!isNullOrEmpty(scriptFile))   //user-defined parser as fallback parser
                mainParser.setFallbackParser(loader.createScript(scriptFile, new Binding()));
            return mainParser;
        });
    }
}
