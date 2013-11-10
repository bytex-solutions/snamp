package com.snamp.hosting;

import com.snamp.*;
import com.snamp.adapters.*;
import com.snamp.connectors.*;

import java.io.IOException;
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
     * Represents a map of instantiated management connector.
     */
    public static interface InstantiatedConnectors extends Map<String, ManagementConnector>{

    }

    private static final class InstantiatedConnectorsImpl extends HashMap<String, ManagementConnector> implements InstantiatedConnectors{
        public InstantiatedConnectorsImpl(){
            super(4);
        }
    }

    @Aggregation
    private Adapter adapter;
    private final Map<String, String> params;
    @Aggregation
    private final InstantiatedConnectors connectors;
    private boolean started;

    private Agent(final Adapter adapter, final Map<String, String> hostingParams){
        super(logger);
        if(adapter == null) throw new IllegalArgumentException("Adapter is not available");
        else this.adapter = adapter;
        this.params = hostingParams != null ? new HashMap<String, String>(hostingParams) : new HashMap<String, String>();
        this.connectors = new InstantiatedConnectorsImpl();
        started = false;
    }

    /**
     * Initializes a new instance of the hosting engine.
     * @param hostingConfig The configuration of the agent.
     * @throws IllegalArgumentException Adapter cannot be resolved.
     */
    public Agent(final AgentConfiguration.HostingConfiguration hostingConfig){
        this(HostingServices.getAdapter(hostingConfig.getAdapterName()), hostingConfig.getHostingParams());
    }

    /**
     * Returns a map of instantiated connectors.
     * @return A map of instantiated connectors.
     */
    public final Map<String, ManagementConnector> getConnectors(){
        return Collections.unmodifiableMap(connectors);
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
    public final void reconfigure(final AgentConfiguration.HostingConfiguration hostingConfig) {
        if(hostingConfig == null) throw new IllegalArgumentException("hostingConfig is null.");
        if(started) throw new IllegalStateException("The agent must be in stopped state.");
        final Adapter ad = HostingServices.getAdapter(hostingConfig.getAdapterName());
        if(ad == null) throw new IllegalStateException("Adapter is not available");
        this.adapter = ad;
        params.clear();
        connectors.clear();
        params.putAll(hostingConfig.getHostingParams());
    }

    private void registerTarget(final AgentConfiguration.ManagementTargetConfiguration targetConfig){
        if(targetConfig == null){
            logger.info("Unexpected empty management target configuration");
            return;
        }
        //loads the management connector
        final ManagementConnector connector;
        if(connectors.containsKey(targetConfig.getConnectionType())){
            connector = connectors.get(targetConfig.getConnectionType());
        }
        else {
            final ManagementConnectorFactory factory = HostingServices.getManagementConnectorFactory(targetConfig.getConnectionType());
            if(factory == null){
                logger.warning(String.format("Management connector %s is not installed.", targetConfig.getConnectionType()));
                return;
            }
            connector = factory.newInstance(targetConfig.getConnectionString(), targetConfig.getAdditionalElements());
            if(connector == null){
                logger.warning(String.format("Management connector %s is not installed.", targetConfig.getConnectionType()));
                return;
            }
        }
        //register attributes
        adapter.exposeAttributes(connector, targetConfig.getNamespace(), targetConfig.getAttributes());
        //register events
        adapter.exposeEvents(connector, targetConfig.getEvents());
    }

    /**
     * Starts the hosting of the instantiated adapter and connectors.
     * @param targets The configuration of connectors.
     * @return {@literal true}, if agent is started successfully; otherwise, {@literal false}.
     * @throws IOException The adapter cannot be instantiated. See logs for more information.
     */
    public final boolean start(final Map<String, AgentConfiguration.ManagementTargetConfiguration> targets) throws IOException {
        if(started) return false;
        else if(targets == null) return start(new HashMap<String, AgentConfiguration.ManagementTargetConfiguration>());
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
        return adapter.stop(true);
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
     */
    @Override
    public void close() throws Exception{
        connectors.clear();
        params.clear();
        adapter.close();
    }
}
