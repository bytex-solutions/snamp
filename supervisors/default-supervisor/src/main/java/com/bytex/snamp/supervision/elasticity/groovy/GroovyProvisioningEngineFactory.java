package com.bytex.snamp.supervision.elasticity.groovy;

import com.bytex.snamp.scripting.groovy.FileBasedGroovyScriptEngine;

import java.io.IOException;
import java.util.Properties;

/**
 * Instantiates {@link GroovyProvisioningEngine} in Groovy sandbox.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GroovyProvisioningEngineFactory extends FileBasedGroovyScriptEngine<GroovyProvisioningEngine> {
    public GroovyProvisioningEngineFactory(final ClassLoader rootClassLoader, final Properties properties, final String path) throws IOException {
        super(rootClassLoader, properties, GroovyProvisioningEngine.class, path);
    }

    @Override
    protected void interceptCreate(final GroovyProvisioningEngine script) {
        script.setBundleContext(getBundleContext());
    }
}
