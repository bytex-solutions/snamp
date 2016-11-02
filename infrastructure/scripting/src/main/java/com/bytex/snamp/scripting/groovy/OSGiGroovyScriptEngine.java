package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.internal.Utils;
import com.google.common.base.StandardSystemProperty;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;


import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents base class for Groovy script engine.
 * @param <B> Type of base script class.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class OSGiGroovyScriptEngine<B extends Script> extends GroovyScriptEngine {
    protected final Binding rootBinding;
    private final Class<B> baseScriptClass;

    public OSGiGroovyScriptEngine(final ClassLoader rootClassLoader,
                                  final Properties properties,
                                  final Class<B> baseScriptClass,
                                  final String... paths) throws IOException {
        super(paths, rootClassLoader);
        setupCompilerConfiguration(getConfig(), properties, baseScriptClass);
        rootBinding = new Binding();
        this.baseScriptClass = Objects.requireNonNull(baseScriptClass);
    }

    private static void setupCompilerConfiguration(final CompilerConfiguration config,
                                                   final Properties properties,
                                                   final Class<?> baseScriptClass){
        config.configure(properties);
        config.setScriptBaseClass(baseScriptClass.getName());
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

    /**
     * Creates a Script with a given scriptName and binding.
     *
     * @param scriptName name of the script to run
     * @param binding    the binding to pass to the script
     * @return the script object
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException   if there is a problem parsing the script
     */
    @Override
    public final B createScript(final String scriptName, final Binding binding) throws ResourceException, ScriptException {
        return createScript(scriptName, binding, baseScriptClass);
    }

    /**
     * Creates a Script with a given scriptName and binding.
     *
     * @param scriptName name of the script to run
     * @param binding    the binding to pass to the script
     * @param baseScriptClass Parent class of Groovy script.
     * @return the script object
     * @throws ResourceException if there is a problem accessing the script
     * @throws ScriptException   if there is a problem parsing the script
     */
    public <C extends B> C createScript(final String scriptName, final Binding binding, final Class<C> baseScriptClass) throws ResourceException, ScriptException{
        final Script result;
        final Binding bindingUnion = concatBindings(rootBinding, binding);
        try {
            result = Utils.callWithContextClassLoader(getGroovyClassLoader(), () -> super.createScript(scriptName, bindingUnion));
        } catch (final ResourceException | ScriptException e){
            throw e;
        } catch (final Exception e) {
            throw new ScriptException(e);
        }
        return baseScriptClass.cast(result);
    }

    public static Binding concatBindings(final Binding first, final Binding... other){
        return ForwardingBinding.create(first, other);
    }

    /**
     * Sets value of the global variable visible to all scripts.
     * @param name The name of the global variable.
     * @param value The value of the global variable.
     * @see #getGlobalVariable(String)
     */
    public final void setGlobalVariable(final String name, final Object value){
        rootBinding.setVariable(name, value);
    }

    /**
     * Gets value of the global variable.
     * @param name The name of the global variable.
     * @return The value of the global variable.
     * @see #setGlobalVariable(String, Object)
     */
    public final Object getGlobalVariable(final String name){
        return rootBinding.getVariable(name);
    }
}
