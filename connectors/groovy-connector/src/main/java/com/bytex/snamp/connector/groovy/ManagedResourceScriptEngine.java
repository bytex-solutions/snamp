package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;
import com.bytex.snamp.scripting.groovy.ScriptingAPISupport;
import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngine extends OSGiGroovyScriptEngine<ManagedResourceScript> implements AttributeConnector, EventConnector {
    private static final String GROOVY_FILE_EXT = ".groovy";
    private static final String RESOURCE_NAME_VAR = "resourceName";

    public ManagedResourceScriptEngine(final String resourceName,
                                       final Logger logger,
                                       final ClassLoader rootClassLoader,
                                       final Properties properties,
                                       final String... paths) throws IOException {
        super(rootClassLoader, properties, ManagedResourceScript.class, paths);
        getGlobalVariables().setVariable(RESOURCE_NAME_VAR, resourceName);
        ScriptingAPISupport.setLogger(getGlobalVariables(), logger);
    }

    private ManagedResourceAttributeScript loadAttribute(final String scriptFile, final Binding environment) throws ResourceException, ScriptException {
        final ManagedResourceAttributeScript result = createScript(scriptFile, environment, ManagedResourceAttributeScript.class);
        result.run();
        return result;
    }

    private ManagedResourceEventScript loadEvent(final String scriptFile,
                                  final NotificationEmitter emitter,
                                  final Binding environment) throws ResourceException, ScriptException{
        final ManagedResourceEventScript result = createScript(scriptFile, environment, ManagedResourceEventScript.class);
        result.setEmitter(emitter);
        result.run();
        return result;
    }

    @Override
    public AttributeAccessor loadAttribute(final String scriptFile) throws ResourceException, ScriptException {
        return loadAttribute(scriptFile, new Binding());
    }

    @Override
    public AttributeAccessor loadAttribute(final String attributeName, final AttributeDescriptor descriptor) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getName(attributeName).concat(GROOVY_FILE_EXT);
        return loadAttribute(scriptFile, new Binding(environment));
    }

    @Override
    public NotificationEmitter loadEvent(final String scriptFile,
                                         final NotificationEmitter realEmitter) throws ResourceException, ScriptException{
        return loadEvent(scriptFile, realEmitter, new Binding());
    }

    @Override
    public NotificationEmitter loadEvent(final String notifType,
                                         final NotificationDescriptor descriptor,
                                         final NotificationEmitter realEmitter) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getName(notifType).concat(GROOVY_FILE_EXT);
        return loadEvent(scriptFile, realEmitter, new Binding(environment));
    }

    public ManagedResourceInfo init(final String initScript,
                     final Map<String, ?> initParams) throws ResourceException, ScriptException {
        final ManagedResourceInitializationScript result = createScript(initScript, new Binding(initParams), ManagedResourceInitializationScript.class);
        result.setContext(getClass().getClassLoader());
        result.run();
        return result;
    }
}
