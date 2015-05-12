package com.itworks.snamp.connectors.groovy;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.jmx.DescriptorUtils;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagementScriptEngine extends Binding implements AttributeConnector {
    private final GroovyScriptEngine attributeLoader;
    private static final String GROOVY_FILE_EXT = ".groovy";
    private final GroovyScriptEngine initScriptLoader;

    public ManagementScriptEngine(final Properties properties,
                                  final String... paths) throws IOException {
        attributeLoader = new GroovyScriptEngine(paths, getClass().getClassLoader());
        attributeLoader.getConfig().configure(properties);
        attributeLoader.getConfig().setScriptBaseClass(AttributeScript.class.getName());

        initScriptLoader = new GroovyScriptEngine(paths, getClass().getClassLoader());
        initScriptLoader.getConfig().configure(properties);
        initScriptLoader.getConfig().setScriptBaseClass(InitializationScript.class.getName());
    }

    public ManagementScriptEngine(final String... paths) throws IOException {
        this(new Properties(), paths);
    }

    private AttributeScript loadAttribute(final String scriptFile,
                                          final Map<String, ?> environment) throws ResourceException, ScriptException{
        return (AttributeScript)attributeLoader.createScript(scriptFile, BackedBinding.create(this, environment));
    }

    @Override
    public AttributeScript loadAttribute(final String scriptFile) throws ResourceException, ScriptException {
        return loadAttribute(scriptFile, ImmutableMap.<String, Object>of());
    }

    @Override
    public AttributeScript loadAttribute(final AttributeDescriptor descriptor) throws ResourceException, ScriptException {
        final Map<String, ?> environment = DescriptorUtils.toMap(descriptor);
        final String scriptFile = descriptor.getAttributeName() + GROOVY_FILE_EXT;
        return loadAttribute(scriptFile, environment);
    }

    public InitializationScript init(final String initScript,
                     final Map<String, ?> initParams) throws ResourceException, ScriptException {
        final InitializationScript script = (InitializationScript)initScriptLoader.createScript(initScript, BackedBinding.create(this, initParams));
        script.run();
        return script;
    }
}
