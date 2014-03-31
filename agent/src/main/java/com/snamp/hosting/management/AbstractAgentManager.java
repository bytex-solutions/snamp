package com.snamp.hosting.management;

import com.snamp.*;

import static com.snamp.ConcurrentResourceAccess.Action;

import com.snamp.core.AbstractPlatformService;
import com.snamp.internal.MethodThreadSafety;
import com.snamp.internal.ThreadSafety;
import com.snamp.hosting.HostingContext;
import net.xeoh.plugins.base.annotations.Capabilities;

import java.util.logging.Logger;

/**
 * Represents an abstract class for implementing custom SNAMP managers.
 * <p>
 *     The inherited class must be annotated with {@link net.xeoh.plugins.base.annotations.PluginImplementation},
 *     and have parameterless constructor.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public abstract class AbstractAgentManager extends AbstractPlatformService implements AgentManager {
    private final String managerName;
    private final ConcurrentResourceAccess<HostingContext> contextHolder;
    private boolean started;

    /**
     * Initializes a new SNAMP manager.
     * @param managerName The name of the manager plug-in.
     */
    protected AbstractAgentManager(final String managerName){
        super(getLogger(managerName));
        this.managerName = managerName;
        this.contextHolder = new ConcurrentResourceAccess<>(null);
        started = false;
    }

    /**
     * Returns the logging infrastructure associated with the specified SNAMP manager.
     * @param managerName The name of the manager.
     * @return An instance of the logger associated with the specified SNAMP manager.
     */
    public static final Logger getLogger(final String managerName){
        return Logger.getLogger(String.format("snamp.managers.%s.log", managerName));
    }

    /**
     * Starts this manager.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected void startCore(final HostingContext context){

    }

    /**
     * Reads the aggregated object from the hosting context.
     * @param reader Hosting context reader.
     * @param <T> Type of the reading result.
     * @param <E> Type of the exception that can be thrown by the reader.
     * @return The object obtained from the hosting context.
     * @throws E
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected final <T, E extends Throwable> T readContext(final Action<HostingContext, T, E> reader) throws E{
        return contextHolder.read(reader);
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected final <T> T readContext(final ConcurrentResourceAccess.ConsistentAction<HostingContext, T> reader) {
        return contextHolder.read(reader);
    }

    /**
     * Starts the manager.
     *
     * @param context SNAMP hosting context.
     */
    @Override
    @ThreadSafety(MethodThreadSafety.LOOP)
    public final void start(final HostingContext context) {
        contextHolder.changeResource(context);
        startCore(context);
    }

    /**
     * Stops the manager.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    protected void stopCore(){

    }

    /**
     * Stops the agent.
     *
     * @return {@literal true} if manager is stopped successfully; otherwise, {@literal false}.
     */
    @Override
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public final boolean stop() {
        if(!started) return false;
        stopCore();
        contextHolder.changeResource((HostingContext)null);
        return true;
    }

    /**
     * Returns an array of plug-in capabilities.
     * @return An array of plug-in capabilities.
     */
    @Capabilities
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public final String[] capabilities(){
        return makeCapabilities(managerName);
    }

    /**
     * Creates a new array of capabilities for JSPF infrastructure, you should not use this method directly
     * in your code.
     * @param managerName The name of the manager.
     * @return An array of plug-in capabilities.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    public static String[] makeCapabilities(final String managerName){
        return new String[]{
                String.format("manager:%s", managerName)
        };
    }
}
