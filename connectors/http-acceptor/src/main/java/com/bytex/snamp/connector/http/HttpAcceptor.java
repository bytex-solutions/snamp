package com.bytex.snamp.connector.http;

import com.bytex.snamp.ImportClass;
import com.bytex.snamp.configuration.ManagedResourceInfo;
import com.bytex.snamp.connector.dsp.DataStreamConnector;
import com.bytex.snamp.connector.dsp.groovy.GroovyNotificationParser;
import com.bytex.snamp.connector.dsp.groovy.GroovyNotificationParserLoader;
import groovy.grape.GrabAnnotationTransformation;
import groovy.lang.Binding;

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
final class HttpAcceptor extends DataStreamConnector {

    HttpAcceptor(final String resourceName, final ManagedResourceInfo configuration) {
        super(resourceName, configuration, HttpConnectorConfigurationDescriptionProvider.getInstance());
    }

    @Override
    protected GroovyNotificationParser createNotificationParser(final String resourceName, final ManagedResourceInfo parameters) {
        return callUnchecked(() -> {
            final URL[] path = HttpConnectorConfigurationDescriptionProvider.getInstance().parseScriptPath(parameters);
            //load standard HTTP parser for measurements
            GroovyNotificationParserLoader loader = new GroovyNotificationParserLoader(this, parameters, true, path);
            final GroovyNotificationParser mainParser = loader.createScript("MeasurementParser.groovy", new Binding());
            //load user-defined parser
            final String scriptFile = HttpConnectorConfigurationDescriptionProvider.getInstance().parseScriptFile(parameters);
            if (!isNullOrEmpty(scriptFile))   //user-defined parser as fallback parser
                mainParser.setFallbackParser(loader.createScript(scriptFile, new Binding()));
            mainParser.setInstanceName(resourceName);
            mainParser.setComponentName(parameters.getGroupName());
            return mainParser;
        });
    }
}
