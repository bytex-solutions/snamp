package com.snamp.hosting;

import com.snamp.connectors.ManagementConnector;
import org.snmp4j.agent.*;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

/**
 * Represents agent host.
 * @author roman
 */
final class AgentHostingSandbox implements AutoCloseable {
    private final AgentConfiguration configuration;
    private SnmpAgent snmp;

    /**
     * Initializes a new instance of the agent host.
     * @param configuration The custom agent configuration.
     */
    private AgentHostingSandbox(final AgentConfiguration configuration){
        if(configuration == null) throw new IllegalArgumentException("configuration is null.");
        this.configuration = configuration;
    }

    private void repl_pause(){
        if(snmp == null) {
            System.err.println("Host is terminated.");
            return;
        }
        else switch (snmp.getAgentState()){
            case SnmpAgent.STATE_RUNNING: snmp.stop(); return;
            default: System.out.println("Agent is already paused.");
        }
    }

    private void repl_resume() throws IOException{
        if(snmp == null) {
            System.err.println("Host is terminated");
            return;
        }
        else switch (snmp.getAgentState()){
            case SnmpAgent.STATE_STOPPED: snmp.start(); return;
            default: System.out.println("Agent is already resumed.");
        }
    }

    private void repl_restart() throws IOException {
        snmp.stop();
        start(true);
    }

    /**
     * Executes Read-Evaluation-Print loop.
     */
    private void repl() throws IOException{
        //run main loop
        try(final BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
            processNexCommand:
            switch (br.readLine()){
                case "?":
                case "help":
                case "uptime":
                    System.out.println(ManagementFactory.getRuntimeMXBean().getUptime());
                    break processNexCommand;
                case "shutdown":
                    snmp.stop();
                    return;
                case "pause":
                    repl_pause();
                    break processNexCommand;
                case "resume":
                    repl_resume();
                    break processNexCommand;
                case "restart":
                    repl_restart();
                    return;
                default:
                    System.out.println("Unsupported command.");
                    break processNexCommand;
            }
        }
    }

    /**
     * Launches the host.
     * @param interracial {@literal true} to start command-line session; otherwise, {@literal false}.
     */
    public void start(final boolean interracial) throws IOException{
        savePid();
        this.snmp = startAgent(configuration.getAgentHostingConfig());
        //registers attributes
        for (final AgentConfiguration.ManagementTargetConfiguration target : configuration.getTargets().values()) {
            registerManagementTarget(snmp, target);
        }
        if(interracial) repl();
    }

    /**
     * Сохранить id процесса в файл чтобы в последствии иметь возможность
     * обратиться к нему
     */
    private static void savePid() throws IOException {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(pid.indexOf("[") == -1 ? 0 : pid.indexOf("[") + 1,
                pid.indexOf("@"));
        System.out.printf("Process ID: %s\n", pid);
        final File file = new File("jmx2snmp.pid");
        file.createNewFile();
        final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            bw.write(pid);
        } finally {
            bw.close();
        }
    }

    private static SnmpAgent startAgent(final AgentConfiguration.HostingConfiguration config) throws IOException{
        final SnmpAgent ag = new SnmpAgent(String.format("%s/%s", config.getAddress(), config.getPort()));
        System.out.printf("SNAMP Started at %s:%s\n", config.getAddress(), config.getPort());
        ag.start();
        return ag;
    }

    private static void registerManagementTarget(final SnmpAgent ag, final AgentConfiguration.ManagementTargetConfiguration target){
        System.out.printf("Registering %s\n", target.getConnectionString());
        final ManagementConnector connector = HostingServices.createConnector(target);
        if(connector == null){
            System.err.printf("Connector '%s' is not supported", target.getConnectionType());
            return;
        }
        final SnmpTypeSystemBridge bridge = new SnmpTypeSystemBridge(connector);
        //register attributes
        final Collection<ManagedObject> managedObjects = new ArrayList<ManagedObject>();
        for(final Map.Entry<String, AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> attribute: target.getAttributes().entrySet()){
            System.out.printf("Registering attribute object %s\n", attribute.getValue().getAttributeName());
            final ManagedObject mo = bridge.connectAttribute(String.format("%s.%s", target.getNamespace(), attribute.getKey()), attribute.getValue().getAttributeName(), attribute.getValue().getAdditionalElements(), attribute.getValue().getReadWriteTimeout());
            if(mo == null) continue;
            else managedObjects.add(mo);
        }
        try {
            ag.registerManagedObjects(target.getNamespace(), managedObjects);
        }
        catch (final DuplicateRegistrationException e) {
            System.err.println(e);
        }
    }

    /**
     * Executes the agent in the caller process.
     * @param configuration The hosting configuration.
     * @param interracial {@literal true} to start command-line session; otherwise, {@literal false}.
     * @return An instance of the hosting sandbox (it is not useful for interracial mode).
     */
    public static AutoCloseable start(final AgentConfiguration configuration, final boolean interracial) throws IOException{
        if(interracial) try(final AgentHostingSandbox hosting = new AgentHostingSandbox(configuration)){
            hosting.start(true);
            return hosting;
        }
        else{
            final AgentHostingSandbox hosting = new AgentHostingSandbox(configuration);
            hosting.start(false);
            return hosting;
        }
    }

    public static void main(String[] args) throws IOException {
        //prepare startup arguments
        switch (args.length){
            case 1: args = new String[]{args[0], ""}; break;
            case 2: break;
            default:
                System.out.println("java snamp config-file");
                System.out.println("Example: java snamp mon.yaml");
                return;
        }
        start(ConfigurationFileFormat.load(args[0], args[1]), true);
    }

    /**
     * Releases all resources associated
     */
    @Override
    public void close() {
        if(snmp != null) snmp.stop();
    }
}
