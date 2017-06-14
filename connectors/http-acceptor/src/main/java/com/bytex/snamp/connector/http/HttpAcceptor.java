package com.bytex.snamp.connector.http;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.dataStream.UnicastDataStreamConnector;
import com.bytex.snamp.connector.dataStream.groovy.GroovyNotificationParser;
import com.bytex.snamp.connector.dataStream.groovy.GroovyNotificationParserLoader;
import groovy.grape.GrabAnnotationTransformation;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents HTTP acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ImportClass(GrabAnnotationTransformation.class)
final class HttpAcceptor extends UnicastDataStreamConnector {

    HttpAcceptor(final String resourceName, final ManagedResourceInfo configuration) {
        super(resourceName, configuration, HttpConnectorConfigurationDescriptionProvider.getInstance());
    }

    private GroovyNotificationParser createNotificationGroovyParser() throws ResourceException, ScriptException, IOException {
        final URL[] path = HttpConnectorConfigurationDescriptionProvider.getInstance().parseScriptPath(getConfiguration());
        //load standard HTTP parser for measurements
        GroovyNotificationParserLoader loader = new GroovyNotificationParserLoader(this, getConfiguration(), true, path);
        final GroovyNotificationParser mainParser = loader.createScript("MeasurementParser.groovy", new Binding());
        //load user-defined parser
        final String scriptFile = HttpConnectorConfigurationDescriptionProvider.getInstance().parseScriptFile(getConfiguration());
        if (!isNullOrEmpty(scriptFile))   //user-defined parser as fallback parser
            mainParser.setFallbackParser(loader.createScript(scriptFile, new Binding()));
        mainParser.setInstanceName(getInstanceName());
        mainParser.setComponentName(getGroupName());
        return mainParser;
    }

    @Override
    protected GroovyNotificationParser createNotificationParser() {
        return callUnchecked(this::createNotificationGroovyParser);
    }
}
