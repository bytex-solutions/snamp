package com.bytex.snamp.scripting;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.LazyReference;
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
    private final LazyReference<ForwardingScriptEngine> cachedScriptEngine;

    OSGiScriptEngineFactory(final ScriptEngineFactory factory, final ClassLoader contextClassLoader){
        this.factory = Objects.requireNonNull(factory);
        this.contextClassLoader = Objects.requireNonNull(contextClassLoader);
        this.cachedScriptEngine = LazyReference.weak();
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

    private ForwardingScriptEngine createScriptEngine() {
        final ScriptEngine engine;
        try (final SafeCloseable ignored = Utils.withContextClassLoader(contextClassLoader)) {
            engine = factory.getScriptEngine();
        }

        return new ForwardingScriptEngine() {
            @Override
            protected ScriptEngine delegate() {
                return engine;
            }

            @Override
            public OSGiScriptEngineFactory getFactory() {
                return OSGiScriptEngineFactory.this;
            }
        };
    }

    @Override
    public ForwardingScriptEngine getScriptEngine() {
        return cachedScriptEngine.get(this, OSGiScriptEngineFactory::createScriptEngine);
    }
}
