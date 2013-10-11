package com.snamp.hosting;

import com.snamp.adapters.*;
import com.snamp.connectors.*;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;

/**
 * Represents agent host.
 * @author roman
 */
public final class Agent implements AutoCloseable {
    private static final Logger log = Logger.getLogger("snamp.log");
    private final Adapter adapter;
    private final Map<String, String> params;
    private final Map<String, ManagementConnector> connectors;
    private boolean started;

    private Agent(final Adapter adapter, final Map<String, String> hostingParams){
        if(adapter == null) throw new IllegalArgumentException("Adapter is not available");
        else this.adapter = adapter;
        this.params = hostingParams != null ? new HashMap<String, String>(hostingParams) : new HashMap<String, String>();
        this.connectors = new HashMap<>(4);
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

    private void registerTarget(final AgentConfiguration.ManagementTargetConfiguration targetConfig){
        if(targetConfig == null){
            log.fine("Unexpected empty management target configuration");
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
                log.warning(String.format("Management connector %s is not installed.", targetConfig.getConnectionType()));
                return;
            }
            connector = factory.newInstance(targetConfig.getConnectionString(), targetConfig.getAdditionalElements());
            if(connector == null){
                log.warning(String.format("Management connector %s is not installed.", targetConfig.getConnectionType()));
                return;
            }
        }
        adapter.exposeAttributes(connector, targetConfig.getNamespace(), targetConfig.getAttributes());
    }

    public boolean start(final Map<String, AgentConfiguration.ManagementTargetConfiguration> targets) throws IOException {
        if(started) return false;
        for(final String targetName: targets.keySet()){
            log.fine(String.format("Registering %s management target", targetName));
            registerTarget(targets.get(targetName));
        }
        adapter.start(params);
        return true;
    }

    /**
     * Executes the agent in the caller process.
     * @param configuration The hosting configurtion.
     * @return An instance of the hosting sandbox (it is not useful for interracial mode).
     */
    private static void start(final AgentConfiguration configuration) throws Exception{
        Agent engine = null;
        try{
            engine = new Agent(configuration.getAgentHostingConfig());
            engine.start(configuration.getTargets());
        }
        catch (final Exception e){
            log.log(Level.SEVERE, "Unable to start SNAMP", e);
            engine = null;
        }
        finally {
            if(engine != null) engine.close();
        }
    }

    public static void main(String[] args) throws Exception {
        //prepare startup arguments
        switch (args.length){
            case 1: args = new String[]{args[0], ""}; break;
            case 2: break;
            default:
                System.out.println("java snamp config-file");
                System.out.println("Example: java snamp mon.yaml");
                return;
        }
        start(ConfigurationFileFormat.load(args[0], args[1]));
    }

    /**
     * Releases all resources associated
     */
    @Override
    public void close() throws Exception{
        connectors.clear();
        params.clear();
        adapter.close();
    }
}
