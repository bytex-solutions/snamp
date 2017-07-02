package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.scripting.groovy.FileBasedGroovyScriptEngine;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngine extends FileBasedGroovyScriptEngine<ManagedResourceScriptlet> {
    private final boolean isDiscovery;
    private final String resourceName;

    public ManagedResourceScriptEngine(final String resourceName,
                                       final ClassLoader rootClassLoader,
                                       final boolean isDiscovery,
                                       final Properties properties,
                                       final String path) throws IOException {
        super(rootClassLoader, properties, ManagedResourceScriptlet.class, path);
        this.resourceName = resourceName;
        this.isDiscovery = isDiscovery;
    }

    @Override
    protected void interceptCreate(final ManagedResourceScriptlet script) {
        script.setDiscovery(isDiscovery);
        script.setBundleContext(getBundleContext());
        script.setResourceName(resourceName);
    }
}
