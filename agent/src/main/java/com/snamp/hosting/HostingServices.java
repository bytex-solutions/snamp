package com.snamp.hosting;

import com.snamp.FileExtensionFilter;
import com.snamp.adapters.*;
import com.snamp.connectors.*;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;
import net.xeoh.plugins.base.util.uri.ClassURI;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Represents internal hosting services and routines.
 * @author roman
 */
final class HostingServices {
    private static final PluginManager manager;
    private static final Logger log;

    private HostingServices(){

    }

    public static ManagementConnector createConnector(final AgentConfiguration.ManagementTargetConfiguration target){
        if(target == null) throw new IllegalArgumentException("target is null.");
        final ManagementConnectorFactory factory = getManagementConnectorFactory(target.getConnectionType());
        if(factory == null){
            log.severe(String.format("Unsupported management connector '%s'", target.getConnectionType()));
            return null;
        }
        return factory.newInstance(target.getConnectionString(), target.getAdditionalElements());
    }

    /**
     * Represents name of the system property that contains path to the folder with SNAMP plugins.
     */
    public static final String PLUGINS_DIR = "com.snamp.plugindir";

    /**
     * Returns a directory with plugins.
     * @return A directory with plugins.
     */
    public static File getPluginsDirectory(){
        return new File(System.getProperty(PLUGINS_DIR, "plugins"));
    }

    static {
        log = Logger.getLogger("snamp.log");
        manager = PluginManagerFactory.createPluginManager();
        //load standard plug-ins
        manager.addPluginsFrom(URI.create("classpath://com.snamp.connectors.**"));
        manager.addPluginsFrom(URI.create("classpath://com.snamp.adapters.**"));
        //load external plugins
        final File pluginDir = getPluginsDirectory();
        if(pluginDir.exists() && pluginDir.isDirectory())
            for(final File plugin: pluginDir.listFiles(new FileExtensionFilter(".jar")))
                if(plugin.isFile()) manager.addPluginsFrom(plugin.toURI());
        else log.severe("No plugins are loaded.");
    }

    /**
     * Registers a new management connector factory.
     * @param connectorFactory The type of the management connector factory to register.
     */
    public static void registerManagementConnectorFactory(final Class<? extends ManagementConnectorFactory> connectorFactory){
        if(connectorFactory == null) throw new IllegalArgumentException("connectorFactory is null.");
        manager.addPluginsFrom(new ClassURI(connectorFactory).toURI());
    }

    public static void registerAdapter(final Class<? extends Adapter> adapterImpl){
        if(adapterImpl == null) throw new IllegalArgumentException("adapterImpl is null.");
        manager.addPluginsFrom(new ClassURI(adapterImpl).toURI());
    }

    public static Adapter getAdapter(final String adapterName){
        return manager.getPlugin(Adapter.class, new OptionCapabilities(AdapterBase.makeCapabilities(adapterName)));
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
