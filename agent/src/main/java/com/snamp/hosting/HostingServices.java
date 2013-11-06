package com.snamp.hosting;

import com.snamp.FileExtensionFilter;
import com.snamp.adapters.*;
import com.snamp.connectors.*;
import com.snamp.hosting.management.*;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;

import java.io.File;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Represents internal hosting services and routines.
 * @author Roman Sakno
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
        manager.addPluginsFrom(URI.create("classpath://com.snamp.hosting.management.**"));
        //load external plugins
        final File pluginDir = getPluginsDirectory();
        if(pluginDir.exists() && pluginDir.isDirectory())
            for(final File plugin: pluginDir.listFiles(new FileExtensionFilter(".jar")))
                if(plugin.isFile()) manager.addPluginsFrom(plugin.toURI());
        else log.severe("No plugins are loaded.");
    }

    public static Adapter getAdapter(final String adapterName){
        return manager.getPlugin(Adapter.class, new OptionCapabilities(AbstractAdapter.makeCapabilities(adapterName)));
    }

    /**
     * Returns the management connector factory already loaded as plugin.
     * @param connectorName The name of the connector.
     * @return A new instance of the management connector factory,
     */
    public static ManagementConnectorFactory getManagementConnectorFactory(final String connectorName){
        return manager.getPlugin(ManagementConnectorFactory.class, new OptionCapabilities(AbstractManagementConnectorFactory.makeCapabilities(connectorName)));
    }

    /**
     * Returns the Agent manager.
     * @param managerName The name of the manager.
     * @return
     */
    public static AgentManager getAgentManager(final String managerName){
        return manager.getPlugin(AgentManager.class, new OptionCapabilities(AgentManagerBase.makeCapabilities(managerName)));
    }

    /**
     * Returns the predefined (through system property) Agent manager.
     * @param defaultIfNotAvailable {@literal true} to return default Agent manager if it is unavailable as plug-in; otherwise, {@link false} for {@literal null}.
     * @return
     */
    public static AgentManager getAgentManager(final boolean defaultIfNotAvailable){
        final AgentManager am =  getAgentManager(AgentManager.MANAGER_NAME);
        return am == null && defaultIfNotAvailable ? new ConsoleAgentManager() : am;
    }
}
