package com.itworks.snamp.adapters;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Resource adapter restart manager useful for managed resource adapter
 * which cannot modify its internal structure on-the-fly
 * when managed resource connector raises {@link com.itworks.snamp.connectors.ResourceEvent}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceAdapterUpdateManager implements AutoCloseable {
    private final long restartTimeout;
    private final String adapterInstanceName;

    private final class ResumeTimer extends Thread {
        private final AtomicLong timeout;

        private ResumeTimer() {
            super("ResourceAdapterRestartManager:" + adapterInstanceName);
            setDaemon(true);
            setPriority(3);
            timeout = new AtomicLong(restartTimeout);
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1);
                } catch (final InterruptedException e) {
                    ResourceAdapterUpdateManager.this.endUpdate();
                    return;
                }
                final long timeout = this.timeout.decrementAndGet();
                //it is time to finalize update of the resource adapter
                if (timeout <= 0) {
                    ResourceAdapterUpdateManager.this.endUpdate();
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
    protected ResourceAdapterUpdateManager(final String adapterInstanceName,
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

    private synchronized void endUpdate(){
        endUpdateCore();
        timer = null;
    }

    /**
     * Finalizes resource adapter update.
     */
    protected abstract void endUpdateCore();

    protected abstract void beginUpdateCore();

    /**
     * Suspends
     * <p>
     *     It is recommended to call this method inside
     *     of {@link com.itworks.snamp.adapters.AbstractResourceAdapter#addFeature(String, javax.management.MBeanFeatureInfo)}
     *     or {@link com.itworks.snamp.adapters.AbstractResourceAdapter#removeFeature(String, javax.management.MBeanFeatureInfo)}
     *     method.
     * @return {@literal true}, if this manager switches from active state to suspended; otherwise, {@literal false}.
     */
    public final synchronized boolean beginUpdate(){
        if(isUpdating()){
            timer.reset();
            return false;
        }
        else {
            beginUpdateCore();
            timer = new ResumeTimer();
            timer.start();
            return true;
        }
    }

    /**
     * Releases all resources associated with this
     * @throws InterruptedException Unable to interrupt background timer.
     */
    @Override
    public final void close() throws InterruptedException {
        Thread t = null;
        synchronized (this) {
            if (timer != null) {
                t = timer;
                t.interrupt();
                timer = null;
            }
        }
        if (t != null) t.join();
    }
}
