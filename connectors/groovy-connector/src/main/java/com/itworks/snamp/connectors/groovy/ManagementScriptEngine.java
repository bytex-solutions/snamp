package com.itworks.snamp.connectors.groovy;

import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.jmx.DescriptorUtils;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagementScriptEngine extends GroovyScriptEngine implements AttributeConnector, EventConnector {
    private static final String GROOVY_FILE_EXT = ".groovy";

    private final Binding rootBinding;

    public ManagementScriptEngine(final ClassLoader rootClassLoader,
                                  final Properties properties,
                                  final String... paths) throws IOException {
        super(paths, rootClassLoader);
        getConfig().configure(properties);
        getConfig().setScriptBaseClass(ManagementScript.class.getName());
        setupClassPath(getConfig());
        rootBinding = new Binding();
    }

    private static void setupClassPath(final CompilerConfiguration config) {
        final List<String> classPath = config.getClasspath();
        final String javaClassPath = StandardSystemProperty.JAVA_CLASS_PATH.value();
        if (!Strings.isNullOrEmpty(javaClassPath)) {
            StringTokenizer tokenizer = new StringTokenizer(javaClassPath, File.pathSeparator);
            while (tokenizer.hasMoreTokens())
                classPath.add(tokenizer.nextToken());
        }
    }

    public ManagementScriptEngine(final ClassLoader rootClassLoader,
                                  final String... paths) throws IOException {
        this(rootClassLoader, new Properties(), paths);
    }

    private synchronized <T extends ManagementScript> T createScript(final String scriptFile,
                                                                 final Map<String, ?> environment,
                                                                 final Class<T> scriptBaseClass) throws ResourceException, ScriptException{
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(getGroovyClassLoader());
        final String previousBaseClass = getConfig().getScriptBaseClass();
        getConfig().setScriptBaseClass(scriptBaseClass.getName());
        try{
            return scriptBaseClass.cast(createScript(scriptFile, BackedBinding.create(rootBinding, environment)));
        }
        finally {
            getConfig().setScriptBaseClass(previousBaseClass);
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }

    private AttributeScript loadAttribute(final String scriptFile,
                                          final Map<String, ?> environment) throws ResourceException, ScriptException {
        final AttributeScript result = createScript(scriptFile, environment, AttributeScript.class);
        result.run();
        return result;
    }

    private EventScript loadEvent(final String scriptFile,
                                  final NotificationEmitter emitter,
                                  final Map<String, ?> environment) throws ResourceException, ScriptException{
        final EventScript result = createScript(scriptFile, environment, EventScript.class);
        result.setEmitter(emitter);
        result.run();
        return result;
    }

    @Override
    public AttributeAccessor loadAttribute(final String scriptFile) throws ResourceException, ScriptException {
        return loadAttribute(scriptFile, ImmutableMap.<String, String>of());
    }

    @Override
    public AttributeAccessor loadAttribute(final AttributeDescriptor descriptor) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getAttributeName() + GROOVY_FILE_EXT;
        return loadAttribute(scriptFile, environment);
    }

    @Override
    public NotificationEmitter loadEvent(final String scriptFile,
                                         final NotificationEmitter realEmitter) throws ResourceException, ScriptException{
        return loadEvent(scriptFile, realEmitter, ImmutableMap.<String, String>of());
    }

    @Override
    public NotificationEmitter loadEvent(final NotificationDescriptor descriptor,
                                         final NotificationEmitter realEmitter) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getNotificationCategory() + GROOVY_FILE_EXT;
        return loadEvent(scriptFile, realEmitter, environment);
    }

    public ManagedResourceInfo init(final String initScript,
                     final Map<String, ?> initParams) throws ResourceException, ScriptException {
        final InitializationScript result = createScript(initScript, initParams, InitializationScript.class);
        result.run();
        return result;
    }
}
