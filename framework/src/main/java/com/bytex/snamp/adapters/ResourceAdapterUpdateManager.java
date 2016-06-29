package com.bytex.snamp.adapters;

import com.bytex.snamp.MethodStub;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Resource adapter restart manager useful for managed resource adapter
 * which cannot modify its internal structure on-the-fly
 * when managed resource connector raises {@link com.bytex.snamp.connectors.ResourceEvent}.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class ResourceAdapterUpdateManager implements AutoCloseable {
    private final long restartTimeout;
    private final String adapterInstanceName;

    private final class ResumeTimer extends Thread {
        private final AtomicLong timeout;
        private final ResourceAdapterUpdatedCallback callback;

        private ResumeTimer(final ResourceAdapterUpdatedCallback callback) {
            super("ResourceAdapterRestartManager:".concat(adapterInstanceName));
            setDaemon(true);
            setPriority(3);
            this.timeout = new AtomicLong(restartTimeout);
            this.callback = firstNonNull(callback, () -> { });
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1);
                } catch (final InterruptedException e) {
                    ResourceAdapterUpdateManager.this.endUpdate(callback);
                    return;
                }
                final long timeout = this.timeout.decrementAndGet();
                //it is time to finalize update of the resource adapter
                if (timeout <= 0) {
                    ResourceAdapterUpdateManager.this.endUpdate(callback);
                    return;
                }
            }
        }

        private void reset() {
            timeout.set(ResourceAdapterUpdateManager.this.restartTimeout);
        }
    }

    private ResumeTimer timer;

    /**
     * Initializes a new restart manager.
     * @param adapterInstanceName The name of the resource adapter instance.
     * @param delay The maximum time (in millis) used to await managed resource events.
     */
    public ResourceAdapterUpdateManager(final String adapterInstanceName,
                                           final long delay){
        this.restartTimeout = delay;
        this.timer = null;
        this.adapterInstanceName = adapterInstanceName;
    }

    /**
     * Determines whether the resource adapter is in updating state.
     * @return {@literal true}, if this adapter is in updating state.
     */
    public final boolean isUpdating(){
        return timer != null;
    }

    private synchronized void endUpdate(final ResourceAdapterUpdatedCallback callback){
        try{
            callback.updated();
        }
        finally {
            timer = null;
        }
    }

    /**
     * Notifies about beginning of the updating process.
     */
    @MethodStub
    protected void beginUpdate(){

    }

    /**
     * Begin or prolong updating of the managed resource adapter internal structure.
     * <p>
     *     It is recommended to call this method inside
     *     of {@link com.bytex.snamp.adapters.AbstractResourceAdapter#addFeature(String, javax.management.MBeanFeatureInfo)}
     *     or {@link com.bytex.snamp.adapters.AbstractResourceAdapter#removeFeature(String, javax.management.MBeanFeatureInfo)}
     *     method.
     * @param callback The callback used to notify about ending of the updating process.
     * @return {@literal true}, if this manager switches from active state to suspended; otherwise, {@literal false}.
     */
    public final synchronized boolean beginUpdate(final ResourceAdapterUpdatedCallback callback){
        if(isUpdating()){
            timer.reset();
            return false;
        }
        else {
            beginUpdate();
            timer = new ResumeTimer(callback);
            timer.start();
            return true;
        }
    }

    /**
     * Continues updating of the managed resource adapter.
     * @return {@literal true}, if this managed is in updating state; otherwise, {@literal false}.
     */
    public final synchronized boolean continueUpdating(){
        if(isUpdating()){
            timer.reset();
            return true;
        }
        else return false;
    }

    /**
     * Releases all resources associated with this
     * @throws Exception Unable to interrupt background timer.
     */
    @Override
    public void close() throws Exception {
        final Thread t;
        //do not place 'synchronized' at method level!!!
        synchronized (this) {
            t = timer;
            timer = null;
            if (t != null)
                t.interrupt();
            else return;
        }
        t.join();
    }

    /**
     * Combines two or more callbacks into the single callback.
     * @param callback The first callback to combine.
     * @param callbacks An array of callbacks to combine with the first callback.
     * @return Combined callbacks.
     */
    public static ResourceAdapterUpdatedCallback combineCallbacks(final ResourceAdapterUpdatedCallback callback,
                                                         final ResourceAdapterUpdatedCallback... callbacks){
        return () -> {
            callback.updated();
            Arrays.stream(callbacks).forEach(ResourceAdapterUpdatedCallback::updated);
        };
    }
}
