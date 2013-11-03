package com.snamp.hosting.management;

import com.snamp.ConcurrentResourceAccess;

import static com.snamp.ConcurrentResourceAccess.ConsistentReader;
import static com.snamp.ConcurrentResourceAccess.Reader;

import com.snamp.MethodThreadSafety;
import com.snamp.ThreadSafety;
import com.snamp.hosting.HostingContext;
import net.xeoh.plugins.base.annotations.Capabilities;

/**
 * @author roman
 */
public abstract class AgentManagerBase implements AgentManager {
    private final String managerName;
    private final ConcurrentResourceAccess<HostingContext> contextHolder;
    private boolean started;

    protected AgentManagerBase(final String managerName){
        this.managerName = managerName;
        this.contextHolder = new ConcurrentResourceAccess<>(null);
        started = false;
    }

    /**
     * Starts this manager.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected void startCore(final HostingContext context){

    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected final <T, E extends Throwable> T readContext(final Reader<HostingContext, T, E> reader) throws E{
        return contextHolder.read(reader);
    }

    @ThreadSafety(MethodThreadSafety.THREAD_SAFE)
    protected final <T> T readContext(final ConsistentReader<HostingContext, T> reader) {
        return contextHolder.read(reader);
    }

    /**
     * Starts the manager.
     *
     * @param context SNAMP hosting context.
     * @return {@literal true} if manager is started successfully; otherwise, {@literal false}.
     */
    @Override
    @ThreadSafety(MethodThreadSafety.LOOP)
    public final void start(final HostingContext context) {
        contextHolder.changeResource(context);
        startCore(context);
    }

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
