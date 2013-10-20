package com.snamp.hosting;

import com.snamp.TimeSpan;
import com.snamp.adapters.Adapter;
import com.snamp.connectors.ManagementConnectorFactory;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents application startup.
 * @author roman
 */
final class Startup extends ReplServer {
    private static final Map<String, AgentConfiguration.ManagementTargetConfiguration> nullTargetsMap = null;
    private final Agent agnt;
    private final AgentConfiguration configuration;
    private final String configFile;


    public Startup(final String configFile, final String configFormat) throws Exception {
        this.configuration = ConfigurationFileFormat.load(configFormat, this.configFile = configFile);
        try(final InputStream stream = new FileInputStream(configFile)){
            this.agnt = Agent.start(this.configuration);
        }
    }

    private void restart(final PrintStream output){
        output.println(String.format("Stopping the agent: %s", this.agnt.stop()));
        output.println(String.format("Reloading configuration from %s", configFile));
        try(final InputStream input = new FileInputStream(configFile)){
            configuration.load(input);
        }
        catch (final IOException e) {
            output.println(String.format("Failed to reload configuration: %s. Restart agent manually.", e.getLocalizedMessage()));
            return;
        }
        output.println("Reconfiguring the agent.");
        this.agnt.reconfigure(this.configuration.getAgentHostingConfig());
        output.println("Starting the agent.");
        try {
            this.agnt.start(this.configuration.getTargets());
        }
        catch (final IOException e) {
            output.println(String.format("Failed to start the agent; %s", e));
            return;
        }
        output.println("Agent restarted successfully.");
    }

    private static void displayProcessID(final PrintStream output){
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');
        // part before '@' empty (index = 0) / '@' not found (index = -1)
        output.println(index < 1 ? "NOT AVAILABLE": jvmName.substring(0, index));
    }

    private static void displayAgentUptime(final PrintStream output){
        final RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        final TimeSpan uptime = TimeSpan.autoScale(rb.getUptime(), TimeUnit.MILLISECONDS);
        output.println(uptime);
    }

    private static void displayHelp(final PrintStream output){
        try(final InputStream help = ReplServer.class.getResourceAsStream("repl_help.txt")){
            int b = -1;
            while ((b = help.read()) >= 0)
                output.write(b);
        }
        catch (final IOException e) {
            output.println(e.getLocalizedMessage());
        }
    }

    private void pauseAgent(final PrintStream output){
        output.println(String.format("Stopping agent: %s", agnt.stop()));
    }

    private void resumeAgent(final PrintStream output){
        try {
            final Map<String, AgentConfiguration.ManagementTargetConfiguration> nullMap = null;
            output.println(String.format("Starting agent: %s", agnt.start(nullMap)));
        }
        catch (final IOException e) {
            output.println(e.getLocalizedMessage());
        }
    }

    private void displayAgentState(final PrintStream output){
        output.println(agnt.isStarted() ? "STARTED" : "STOPPED");
    }

    @Override
    protected boolean doCommand(final String commmand, final PrintStream output) {
        switch (commmand){
            case "uptime": displayAgentUptime(output); return true;
            case "exit": return false;
            case "pid": displayProcessID(output); return true;
            case "help": displayHelp(output); return true;
            case "state": displayAgentState(output); return true;
            case "restart": restart(output); return true;
            case "pause": pauseAgent(output); return true;
            case "resume": resumeAgent(output); return true;
            default: output.println("Unknown command"); return true;
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
        Agent.start(ConfigurationFileFormat.load(args[0], args[1]));
        //represents REPL server startup.
        final ReplServer repl = new Startup(args[0], args[1]);
        repl.loop(System.in, System.out);
    }
}
