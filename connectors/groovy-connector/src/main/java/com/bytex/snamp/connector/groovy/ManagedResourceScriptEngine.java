package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.scripting.groovy.OSGiGroovyScriptEngine;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ManagedResourceScriptEngine extends OSGiGroovyScriptEngine<ManagedResourceScriptlet> {
    private final boolean isDiscovery;
    private final String resourceName;
    private final Logger logger;

    public ManagedResourceScriptEngine(final String resourceName,
                                       final Logger logger,
                                       final ClassLoader rootClassLoader,
                                       final boolean isDiscovery,
                                       final Properties properties,
                                       final URL... paths) throws IOException {
        super(rootClassLoader, properties, ManagedResourceScriptlet.class, paths);
        this.resourceName = resourceName;
        this.isDiscovery = isDiscovery;
        this.logger = logger;
    }

    @Override
    protected void interceptCreate(final ManagedResourceScriptlet script) {
        script.setDiscovery(isDiscovery);
        script.setBundleContext(getBundleContext());
        script.setLogger(logger);
        script.setResourceName(resourceName);
    }
}
