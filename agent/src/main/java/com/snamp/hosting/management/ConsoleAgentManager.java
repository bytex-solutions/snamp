package com.snamp.hosting.management;

import com.snamp.*;
import com.snamp.hosting.*;
import com.snamp.licensing.LicenseReader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents console-based Agent manager.
 * @author Roman Sakno
 */
public class ConsoleAgentManager extends AbstractAgentManager {
    public static final String MANAGER_NAME = "console";
    private final InputStream input;
    private final PrintStream output;
    private final PrintStream errors;

    protected ConsoleAgentManager(final String managerName, final InputStream is, final PrintStream os, final PrintStream es){
        super(managerName);
        if(is == null) throw new IllegalArgumentException("is is null.");
        else if(os == null) throw new IllegalArgumentException("os is null.");
        else if(es == null) throw new IllegalArgumentException("es is null.");
        input = is;
        output = os;
        errors = es;
    }

    public ConsoleAgentManager(){
        this(MANAGER_NAME, System.in, System.out, System.err);
    }

    /**
     * Starts this manager.
     */
    @Override
    protected final void startCore(final HostingContext context) {
        try(final BufferedReader reader = new BufferedReader(new InputStreamReader(input))){
            while (doCommand(reader.readLine(), output)){
            }
        }
        catch (final IOException e) {
            errors.println(e);
        }
    }

    private static void restart(final Agent agnt, final AgentConfigurationStorage storage, final PrintStream output){
        output.println(String.format("Stopping the agent: %s", agnt.stop()));
        try {
            final AgentConfiguration currentConfig = storage.getStoredAgentConfiguration(AgentConfigurationStorage.TAG_LAST).restore();
            agnt.reconfigure(currentConfig.getAgentHostingConfig());
            output.println("Starting the agent.");
            agnt.start(currentConfig.getTargets());
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
        try(final InputStream help = ConsoleAgentManager.class.getResourceAsStream("repl_help.txt")){
            int b = -1;
            while ((b = help.read()) >= 0)
                output.write(b);
        }
        catch (final IOException e) {
            output.println(e.getLocalizedMessage());
        }
    }

    private static void reloadLicense(final PrintStream output){
        LicenseReader.reloadCurrentLicense();
    }

    private static void pauseAgent(final Agent agnt, final PrintStream output){
        output.println(String.format("Stopping agent: %s", agnt.stop()));
    }

    private static void resumeAgent(final Agent agnt, final PrintStream output){
        try {
            final Map<String, AgentConfiguration.ManagementTargetConfiguration> nullMap = null;
            output.println(String.format("Starting agent: %s", agnt.start(nullMap)));
        }
        catch (final IOException e) {
            output.println(e.getLocalizedMessage());
        }
    }

    private static void displayAgentState(final Agent agnt, final PrintStream output){
        output.println(agnt.isStarted() ? "STARTED" : "STOPPED");
    }

    protected boolean doCommand(final String commmand, final PrintStream output) {
        return readContext(new ConcurrentResourceAccess.ConsistentReader<HostingContext, Boolean>() {
            @Override
            public Boolean read(final HostingContext context) {
                if(context == null) return false;
                else switch (commmand){
                    case "uptime": displayAgentUptime( output); return true;
                    case "exit": return false;
                    case "pid": displayProcessID(output); return true;
                    case "help": displayHelp(output); return true;
                    case "state": displayAgentState(context.queryObject(HostingContext.AGENT), output); return true;
                    case "restart": restart(context.queryObject(HostingContext.AGENT), context.queryObject(HostingContext.CONFIG_STORAGE), output); return true;
                    case "pause": pauseAgent(context.queryObject(HostingContext.AGENT), output); return true;
                    case "resume": resumeAgent(context.queryObject(HostingContext.AGENT), output); return true;
                    case "reloadlic": reloadLicense(output); return true;
                    default: output.println("Unknown command"); return true;
                }
            }
        });
    }

    @Override
    public void close() {

    }
}
