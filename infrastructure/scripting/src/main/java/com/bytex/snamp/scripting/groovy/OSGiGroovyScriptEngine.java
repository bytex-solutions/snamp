package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.internal.Utils;
import com.google.common.base.StandardSystemProperty;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    private final Binding rootBinding;
    private final Class<B> baseScriptClass;

    public OSGiGroovyScriptEngine(final ClassLoader rootClassLoader,
                                  final Properties properties,
                                  final Class<B> baseScriptClass,
                                  final URL... paths) throws IOException {
        super(paths, rootClassLoader);
        setupCompilerConfiguration(getConfig(), properties, baseScriptClass);
        rootBinding = new Binding();
        this.baseScriptClass = Objects.requireNonNull(baseScriptClass);
    }

    /**
     * Gets global variables which will be propagated into script.
     * @return A collection of global variables.
     */
    public final Binding getGlobalVariables(){
        return rootBinding;
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

    protected final BundleContext getBundleContext(){
        final ClassLoader rootClassLoader = getParentClassLoader();
        return rootClassLoader instanceof BundleReference ? ((BundleReference) rootClassLoader).getBundle().getBundleContext() : null;
    }

    @MethodStub
    protected void interceptCreate(final B script){

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
    public synchronized final B createScript(final String scriptName, final Binding binding) throws ResourceException, ScriptException {
        final Script result;
        final Binding bindingUnion = binding == null ? rootBinding : concatBindings(rootBinding, binding);
        try (final SafeCloseable ignored = Utils.withContextClassLoader(getGroovyClassLoader())) {
            result = super.createScript(scriptName, bindingUnion);
        } catch (final ResourceException | ScriptException e) {
            throw e;
        } catch (final Exception e) {
            throw new ScriptException(e);
        }
        final B typedResult = baseScriptClass.cast(result);
        interceptCreate(typedResult);
        return typedResult;
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
    public synchronized final <C extends B> C createScript(final String scriptName, final Binding binding, final Class<C> baseScriptClass) throws ResourceException, ScriptException{
        final Script result;
        final Binding bindingUnion = binding == null ? rootBinding : concatBindings(rootBinding, binding);

        final String previousBaseClass = getConfig().getScriptBaseClass();
        getConfig().setScriptBaseClass(baseScriptClass.getName());
        try(final SafeCloseable ignored = Utils.withContextClassLoader(getGroovyClassLoader())) {
            result = super.createScript(scriptName, bindingUnion);
        } catch (final ResourceException | ScriptException e){
            throw e;
        } catch (final Exception e) {
            throw new ScriptException(e);
        } finally {
            getConfig().setScriptBaseClass(previousBaseClass);
        }
        final C typedResult = baseScriptClass.cast(result);
        interceptCreate(typedResult);
        return typedResult;
    }


    public synchronized final B parseScript(final String text, final Binding binding) {
        final Class<?> scriptImpl = getGroovyClassLoader().parseClass(text);
        return baseScriptClass.cast(InvokerHelper.createScript(scriptImpl, binding));
    }

    public static Binding concatBindings(final Binding first, final Binding... other){
        return ForwardingBinding.create(first, other);
    }
}
