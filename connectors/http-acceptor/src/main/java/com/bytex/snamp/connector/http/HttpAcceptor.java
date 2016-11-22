package com.bytex.snamp.connector.http;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.connector.md.MessageDrivenConnector;
import com.bytex.snamp.connector.md.NotificationParser;
import com.bytex.snamp.connector.md.groovy.GroovyNotificationParserLoader;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import groovy.lang.Binding;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.powerassert.ValueRecorder;
import org.codehaus.groovy.transform.BaseScriptASTTransformation;
import org.codehaus.groovy.vmplugin.v7.IndyInterface;

import java.util.Map;

import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents HTTP acceptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HttpAcceptor extends MessageDrivenConnector {
    //Special array for maven-bundle-plugin for correct import of groovy classes
    @SpecialUse
    private static final Class<?>[] GROOVY_DEPS = {BaseScriptASTTransformation.class, ClassInfo.class, ValueRecorder.class, DefaultGroovyMethods.class, IndyInterface.class};

    HttpAcceptor(final String resourceName, final Map<String, String> parameters) {
        super(resourceName, parameters, HttpConnectorConfigurationDescriptor.getInstance());
    }

    @Override
    protected NotificationParser createNotificationParser(final String resourceName, final NotificationSource source, final Map<String, String> parameters) {
        return callUnchecked(() -> {
            final GroovyNotificationParserLoader loader = new GroovyNotificationParserLoader(this, parameters);
            return loader.createScript("MeasurementParser.groovy", new Binding());
        });
    }
}
