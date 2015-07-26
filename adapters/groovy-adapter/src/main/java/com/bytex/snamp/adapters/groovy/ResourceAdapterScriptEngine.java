package com.bytex.snamp.adapters.groovy;

import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
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
 * Represents script engine for Resource Adapter scripts.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ResourceAdapterScriptEngine extends GroovyScriptEngine {
    private static final Class<ResourceAdapterScript> BASE_SCRIPT_CLASS = ResourceAdapterScript.class;

    private final Binding rootBinding;

    public ResourceAdapterScriptEngine(final ClassLoader rootClassLoader,
                                       final Properties properties,
                                       final String... paths) throws IOException {
        super(paths, rootClassLoader);
        setupCompilerConfiguration(getConfig(), properties);
        rootBinding = new Binding();
    }

    public ResourceAdapterScriptEngine(final ClassLoader rootClassLoader,
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
        config.setScriptBaseClass(BASE_SCRIPT_CLASS.getName());
        config.getOptimizationOptions().put("indy", true);
        setupClassPath(config);
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

    public ResourceAdapterScript createScript(final String scriptFile,
                                                            final Map<String, ?> environment) throws ResourceException, ScriptException {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(getGroovyClassLoader());
        try {
            return BASE_SCRIPT_CLASS.cast(createScript(scriptFile, BackedBinding.create(rootBinding, environment)));
        } finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }
}
