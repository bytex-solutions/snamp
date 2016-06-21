package com.bytex.snamp.scripting;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.internal.Utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;
import java.util.Objects;

/**
 * This is a wrapper class for the ScriptEngineFactory class that deals with context class loader issues
 * It is necessary because engines (at least ruby) use the context classloader to find their resources (i.e., their "native" classes)
 * @author Apache Foundation, Roman Sakno
 */
final class OSGiScriptEngineFactory implements ScriptEngineFactory{
    private final ScriptEngineFactory factory;
    private final ClassLoader contextClassLoader;

    OSGiScriptEngineFactory (final ScriptEngineFactory factory, final ClassLoader contextClassLoader){
        this.factory = Objects.requireNonNull(factory);
        this.contextClassLoader = Objects.requireNonNull(contextClassLoader);
    }

    @Override
    public String getEngineName() {
        return factory.getEngineName();
    }

    @Override
    public String getEngineVersion() {
        return factory.getEngineVersion();
    }

    @Override
    public List<String> getExtensions() {
        return factory.getExtensions();
    }

    @Override
    public String getLanguageName() {
        return factory.getLanguageName();
    }

    @Override
    public String getLanguageVersion() {
        return factory.getLanguageVersion();
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        return factory.getMethodCallSyntax(obj, m, args);
    }

    @Override
    public List<String> getMimeTypes() {
        return factory.getMimeTypes();
    }

    @Override
    public List<String> getNames() {
        return factory.getNames();
    }

    @Override
    public String getOutputStatement(final String toDisplay) {
        return factory.getOutputStatement(toDisplay);
    }

    @Override
    public Object getParameter(final String key) {
        return factory.getParameter(key);
    }

    @Override
    public String getProgram(final String... statements) {
        return factory.getProgram(statements);
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return contextClassLoader != null ?
                Utils.withContextClassLoader(contextClassLoader, new ExceptionalCallable<ScriptEngine, ExceptionPlaceholder>(){
                    @Override
                    public ScriptEngine call() {
                        return factory.getScriptEngine(); //do not replace with lambda, compiler can't understand it
                    }
                }) :
                factory.getScriptEngine();
    }
}
