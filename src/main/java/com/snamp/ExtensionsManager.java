package com.snamp;

import com.snamp.connectors.*;
import net.xeoh.plugins.base.*;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.util.uri.ClassURI;

import java.net.URI;

/**
 * Represents SNAMP extensions loader.
 * @author roman
 */
public final class ExtensionsManager {
    private static final PluginManager manager;

    private ExtensionsManager(){

    }

    static {
        manager = PluginManagerFactory.createPluginManager();
        //load standard plug-ins
        manager.addPluginsFrom(URI.create("classpath://com.snamp.connectors.jmx.JmxConnectorFactory"));
    }

    /**
     * Registers a new management connector factory.
     * @param connectorFactory The type of the management connector factory to register.
     * @param <T> Type of the management connector factory to register.
     */
    public static <T extends ManagementConnectorFactory> void registerManagementConnectorFactory(final Class<T> connectorFactory){
        if(connectorFactory == null) throw new IllegalArgumentException("connectorFactory is null.");
        manager.addPluginsFrom(new ClassURI(connectorFactory).toURI());
    }

    /**
     * Returns the management connector factory already loaded as plugin.
     * @param connectorName The name of the connector.
     * @return A new instance of the management connector factory,
     */
    public static ManagementConnectorFactory getManagementConnectorFactory(final String connectorName){
        return manager.getPlugin(ManagementConnectorFactory.class, new OptionCapabilities(ManagementConnectorFactoryBase.makeCapabilities(connectorName)));
    }
}
