package com.snamp.hosting;

import com.snamp.adapters.*;
import com.snamp.configuration.AgentConfiguration;
import com.snamp.connectors.*;
import com.snamp.core.AbstractPlatformService;
import com.snamp.hosting.management.*;
import com.snamp.internal.*;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.options.getplugin.OptionCapabilities;

import static com.snamp.internal.ReflectionUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.logging.*;

/**
 * Represents SNAMP hosting agent that provides interoperability between adapter and connectors.
 * <p>
 *     This class accepts the SNAMP configuration and instantiates necessary adapters and connectors,
 *     and provides communication between them. An instance of this class should be created once per calling process.
 * </p>
 * <p>
 *     Also, this class is an entry point for embedding SNAMP into your application.<br/>
 *     <pre>{@code
 *     //restores the configuration from the file
 *     final AgentConfiguration config = ConfigurationFormat.YAML.newAgentConfiguration();
 *     config.load(fileStream);
 *     try(final Agent ag = new Agent(config.getHostingConfiguration())){
 *       ag.start(config.getTargets());
 *     }
 *     }</pre>
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Internal
@Lifecycle(InstanceLifecycle.SINGLE_PER_PROCESS)
public final class Agent extends AbstractPlatformService implements AutoCloseable {
    private static final Logger logger = Logger.getLogger("com.snamp");
    /**
     * Represents name of the system property that contains path to the folder with SNAMP plugins.
     */
    public static final String PLUGINS_DIR = "com.snamp.plugindir";

    /**
     * Represents a map of instantiated management connector.
     */
    public static interface InstantiatedConnectors extends Map<String, ManagementConnector>{

    }

    private static final class InstantiatedConnectorsImpl extends HashMap<String, ManagementConnector> implements InstantiatedConnectors{
        public InstantiatedConnectorsImpl(){
            super(4);
        }
    }

    private Adapter adapter;
    private final Map<String, String> params;
    private final InstantiatedConnectors connectors;
    private boolean started;
    private final PluginManager pluginManager;

    private static PluginManager loadPlugins(){
        final PluginManager pluginManager = PluginManagerFactory.createPluginManager();
        //load standard plug-ins
        pluginManager.addPluginsFrom(URI.create("classpath://com.snamp.connectors.**"));
        pluginManager.addPluginsFrom(URI.create("classpath://com.snamp.adapters.**"));
        pluginManager.addPluginsFrom(URI.create("classpath://com.snamp.hosting.management.**"));
        //load external plugins
        final File pluginDir = getPluginsDirectory();
        if(pluginDir.exists() && pluginDir.isDirectory())
            for(final File plugin: pluginDir.listFiles(new FileExtensionFilter("jar")))
                if(plugin.isFile()) pluginManager.addPluginsFrom(plugin.toURI(), new OptionReportAfter());
                else logger.warning("No plugins are loaded.");
        return pluginManager;
    }

    /**
     * Initializes a new instance of the hosting engine.
     * @param hostingConfig The configuration of the agent.
     * @throws IllegalArgumentException Adapter cannot be resolved.
     */
    public Agent(final AgentConfiguration.HostingConfiguration hostingConfig){
        super(logger);
        pluginManager = loadPlugins();
        adapter = getAdapter(pluginManager, hostingConfig.getAdapterName());
        if(adapter == null) throw new IllegalArgumentException("Adapter is not available");
        this.params = new HashMap<>(hostingConfig.getHostingParams());
        this.connectors = new InstantiatedConnectorsImpl();
        started = false;
    }

    /**
     * Returns the adapter instance.
     * @param manager SNAMP plugin manager.
     * @param adapterName The name of the adapter to return.
     * @return An instance of the adapter.
     */
    private static Adapter getAdapter(final PluginManager manager, final String adapterName){
        return manager.getPlugin(Adapter.class, new OptionCapabilities(AbstractAdapter.makeCapabilities(adapterName)));
    }

    /**
     * Returns the adapter instance.
     * @param adapterName The name of the adapter to return.
     * @return An instance of the adapter.
     */
    public final Adapter getAdapter(final String adapterName){
        return getAdapter(pluginManager, adapterName);
    }

    /**
     * Returns the management connector factory already loaded as plugin.
     * @param manager SNAMP plugin manager.
     * @param connectorName The name of the connector.
     * @return A new instance of the management connector factory,
     */
    private static ManagementConnectorFactory getManagementConnectorFactory(final PluginManager manager, final String connectorName){
        return manager.getPlugin(ManagementConnectorFactory.class, new OptionCapabilities(AbstractManagementConnectorFactory.makeCapabilities(connectorName)));
    }

    /**
     * Returns the management connector factory already loaded as plugin.
     * @param connectorName The name of the connector.
     * @return A new instance of the management connector factory,
     */
    public final ManagementConnectorFactory getManagementConnectorFactory(final String connectorName){
        return getManagementConnectorFactory(pluginManager, connectorName);
    }

    private static ManagementConnector createConnector(final PluginManager manager, final AgentConfiguration.ManagementTargetConfiguration target){
        if(target == null) throw new IllegalArgumentException("target is null.");
        final ManagementConnectorFactory factory = getManagementConnectorFactory(manager, target.getConnectionType());
        if(factory == null){
            logger.severe(String.format("Unsupported management connector '%s'", target.getConnectionType()));
            return null;
        }
        return factory.newInstance(target.getConnectionString(), target.getAdditionalElements());
    }

    /**
     * Returns the Agent manager.
     * @param manager SNAMP plugin manager.
     * @param managerName The name of the manager.
     * @return
     */
    private static AgentManager getAgentManager(final PluginManager manager, final String managerName){
        return manager.getPlugin(AgentManager.class, new OptionCapabilities(AbstractAgentManager.makeCapabilities(managerName)));
    }

    /**
     * Returns the Agent manager.
     * @param managerName The name of the manager.
     * @return
     */
    public final AgentManager getAgentManager(final String managerName){
        return getAgentManager(pluginManager, managerName);
    }

    /**
     * Returns the predefined (through system property) Agent manager.
     * @param manager SNAMP plugin manager.
     * @param defaultIfNotAvailable {@literal true} to return default Agent manager if it is unavailable as plug-in; otherwise, {@link false} for {@literal null}.
     * @return A new instance of the SNAMP agent manager.
     */
    private static AgentManager getAgentManager(final PluginManager manager, final boolean defaultIfNotAvailable){
        final AgentManager am =  getAgentManager(manager, System.getProperty(AgentManager.MANAGER_NAME));
        return am == null && defaultIfNotAvailable ? new ConsoleAgentManager() : am;
    }

    /**
     * Returns the predefined (through system property) Agent manager.
     * @param defaultIfNotAvailable {@literal true} to return default Agent manager if it is unavailable as plug-in; otherwise, {@link false} for {@literal null}.
     * @return A new instance of the SNAMP agent manager.
     */
    public final AgentManager getAgentManager(final boolean defaultIfNotAvailable){
        return getAgentManager(pluginManager, defaultIfNotAvailable);
    }

    /**
     * Returns a directory with plugins.
     * @return A directory with plugins.
     */
    public static File getPluginsDirectory(){
        return new File(System.getProperty(PLUGINS_DIR, "plugins"));
    }

    /**
     * Returns a map of instantiated connectors.
     * @return A map of instantiated connectors.
     */
    public final Map<String, ManagementConnector> getConnectors(){
        return Collections.unmodifiableMap(connectors);
    }

    private static void releaseConnectors(final Collection<ManagementConnector> connectors) throws Exception{
        for(final ManagementConnector mc: connectors)
            mc.close();
    }

    /**
     * Reconfigures the agent.
     * <p>
     *     You should call {@link #stop()} before invocation of this method.
     * </p>
     * @param hostingConfig A new configuration of the adapter.
     * @throws IllegalArgumentException hostingConfig is {@literal null}.
     * @throws IllegalStateException Attempts to reconfigure agent in the started state.
     */
    public final void reconfigure(final AgentConfiguration.HostingConfiguration hostingConfig) throws Exception {
        if(hostingConfig == null) throw new IllegalArgumentException("hostingConfig is null.");
        if(started) throw new IllegalStateException("The agent must be in stopped state.");
        final Adapter ad = getAdapter(hostingConfig.getAdapterName());
        if(ad == null) throw new IllegalStateException("Adapter is not available");
        this.adapter = ad;
        params.clear();
        releaseConnectors(connectors.values());
        connectors.clear();
        params.putAll(hostingConfig.getHostingParams());
        System.gc();
    }

    private void registerTarget(final AgentConfiguration.ManagementTargetConfiguration targetConfig){
        if(targetConfig == null){
            logger.info("Unexpected empty management target configuration");
            return;
        }
        //loads the management connector
        final ManagementConnector connector;
        if(connectors.containsKey(targetConfig.getConnectionType()))
            connector = connectors.get(targetConfig.getConnectionType());
        else {
            connector = createConnector(pluginManager, targetConfig);
            if(connector == null) return;
            else connectors.put(targetConfig.getConnectionType(), connector);
        }
        //register attributes
        adapter.exposeAttributes(isolate(connector, AttributeSupport.class), targetConfig.getNamespace(), targetConfig.getAttributes());
        //register events
        if(connector instanceof NotificationSupport && adapter instanceof NotificationPublisher)
            ((NotificationPublisher)adapter).exposeEvents(castAndIsolate(connector, NotificationSupport.class, NotificationSupport.class), targetConfig.getNamespace(), targetConfig.getEvents());
    }

    /**
     * Starts the hosting of the instantiated adapter and connectors.
     * @param targets The configuration of connectors.
     * @return {@literal true}, if agent is started successfully; otherwise, {@literal false}.
     * @throws IOException The adapter cannot be instantiated. See logs for more information.
     */
    public final boolean start(final Map<String, AgentConfiguration.ManagementTargetConfiguration> targets) throws IOException {
        if(started) return false;
        else if(targets == null) return start(Collections.<String, AgentConfiguration.ManagementTargetConfiguration>emptyMap());
        else for(final String targetName: targets.keySet()){
            logger.fine(String.format("Registering %s management target", targetName));
            registerTarget(targets.get(targetName));
        }
        return adapter.start(params);
    }

    /**
     * Stops the hosting of instantiated adapter and connectors.
     * @return {@literal true}, if agent is stopped successfully; otherwise, {@literal false}.
     */
    public final boolean stop(){
        boolean success;
        try {
            success = adapter.stop(false);
            releaseConnectors(connectors.values());
            connectors.clear();
        }
        catch (final Exception e) {
            logger.log(Level.SEVERE, "Unable to stop SNAMP agent", e);
            success = false;
        }
        finally {
            System.gc();
        }
        return success;
    }

    /**
     * Executes the agent in the caller process.
     * @param configuration The hosting configuration.
     * @return An instance of the hosting sandbox (it is not useful for interracial mode).
     */
    static Agent start(final AgentConfiguration configuration){
        try{
            final Agent engine = new Agent(configuration.getAgentHostingConfig());
            engine.start(configuration.getTargets());
            return engine;
        }
        catch (final Exception e){
            logger.log(Level.SEVERE, "Unable to start SNAMP", e);
            return null;
        }
    }

    /**
     * Determines whether the agent is started.
     * @return {@literal true} if this agent is in started state; otherwise, {@literal false}.
     */
    public final boolean isStarted(){
        return started;
    }

    /**
     * Releases all resources associated with this instance of agent.
     * @throws Exception
     */
    @Override
    public void close() throws Exception{
        params.clear();
        adapter.close();
        releaseConnectors(connectors.values());
        connectors.clear();
        pluginManager.shutdown();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @param <T>        Type of the required object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public final <T> T queryObject(final Class<T> objectType) {
        if(objectType == null) return null;
        else if(objectType.isInstance(adapter)) return objectType.cast(adapter);
        else if(objectType.isInstance(connectors)) return objectType.cast(connectors);
        else return null;
    }
}
