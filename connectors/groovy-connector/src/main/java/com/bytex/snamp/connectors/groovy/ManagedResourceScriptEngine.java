package com.bytex.snamp.connectors.groovy;

import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.google.common.base.StandardSystemProperty;
import static com.google.common.base.Strings.isNullOrEmpty;
import com.google.common.collect.ImmutableMap;
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
 * @version 2.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngine extends GroovyScriptEngine implements AttributeConnector, EventConnector {
    private static final String GROOVY_FILE_EXT = ".groovy";

    private final Binding rootBinding;

    public ManagedResourceScriptEngine(final ClassLoader rootClassLoader,
                                       final Properties properties,
                                       final String... paths) throws IOException {
        super(paths, rootClassLoader);
        setupCompilerConfiguration(getConfig(), properties);
        rootBinding = new Binding();
    }

    public ManagedResourceScriptEngine(final ClassLoader rootClassLoader,
                                       final String... paths) throws IOException {
        this(rootClassLoader, new Properties(), paths);
    }

    /**
     * Sets value of the global variable visible to all scripts.
     * @param name The name of the global variable.
     * @param value The value of the global variable.
     * @see #getGlobalVariable(String)
     */
    public void setGlobalVariable(final String name, final Object value){
        rootBinding.setVariable(name, value);
    }

    /**
     * Gets value of the global variable.
     * @param name The name of the global variable.
     * @return The value of the global variable.
     * @see #setGlobalVariable(String, Object)
     */
    public Object getGlobalVariable(final String name){
        return rootBinding.getVariable(name);
    }

    private static void setupCompilerConfiguration(final CompilerConfiguration config,
                                                   final Properties properties){
        config.configure(properties);
        config.setScriptBaseClass(ManagedResourceScript.class.getName());
        config.getOptimizationOptions().put("indy", true);
        setupClassPath(config);
    }

    private static void setupClassPath(final CompilerConfiguration config) {
        final List<String> classPath = config.getClasspath();
        final String javaClassPath = StandardSystemProperty.JAVA_CLASS_PATH.value();
        if (!isNullOrEmpty(javaClassPath)) {
            StringTokenizer tokenizer = new StringTokenizer(javaClassPath, File.pathSeparator);
            while (tokenizer.hasMoreTokens())
                classPath.add(tokenizer.nextToken());
        }
    }

    private synchronized <T extends ManagedResourceScript> T createScript(final String scriptFile,
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

    private ManagedResourceAttributeScript loadAttribute(final String scriptFile,
                                          final Map<String, ?> environment) throws ResourceException, ScriptException {
        final ManagedResourceAttributeScript result = createScript(scriptFile, environment, ManagedResourceAttributeScript.class);
        result.run();
        return result;
    }

    private ManagedResourceEventScript loadEvent(final String scriptFile,
                                  final NotificationEmitter emitter,
                                  final Map<String, ?> environment) throws ResourceException, ScriptException{
        final ManagedResourceEventScript result = createScript(scriptFile, environment, ManagedResourceEventScript.class);
        result.setEmitter(emitter);
        result.run();
        return result;
    }

    @Override
    public AttributeAccessor loadAttribute(final String scriptFile) throws ResourceException, ScriptException {
        return loadAttribute(scriptFile, ImmutableMap.<String, String>of());
    }

    @Override
    public AttributeAccessor loadAttribute(final String attributeName, final AttributeDescriptor descriptor) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getName(attributeName).concat(GROOVY_FILE_EXT);
        return loadAttribute(scriptFile, environment);
    }

    @Override
    public NotificationEmitter loadEvent(final String scriptFile,
                                         final NotificationEmitter realEmitter) throws ResourceException, ScriptException{
        return loadEvent(scriptFile, realEmitter, ImmutableMap.<String, String>of());
    }

    @Override
    public NotificationEmitter loadEvent(final String notifType,
                                         final NotificationDescriptor descriptor,
                                         final NotificationEmitter realEmitter) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getName(notifType).concat(GROOVY_FILE_EXT);
        return loadEvent(scriptFile, realEmitter, environment);
    }

    public ManagedResourceInfo init(final String initScript,
                     final Map<String, ?> initParams) throws ResourceException, ScriptException {
        final ManagedResourceInitializationScript result = createScript(initScript, initParams, ManagedResourceInitializationScript.class);
        result.setContext(getClass().getClassLoader());
        result.run();
        return result;
    }
}
