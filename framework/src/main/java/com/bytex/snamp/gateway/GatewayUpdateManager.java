package com.bytex.snamp.gateway;

import com.bytex.snamp.MethodStub;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Restart manager useful for gateways
 * which cannot modify its internal structure on-the-fly
 * when managed resource connector raises event {@link com.bytex.snamp.connector.ResourceEvent}.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class GatewayUpdateManager implements AutoCloseable {
    private final long restartTimeout;
    private final String gatewayInstance;

    private final class ResumeTimer extends Thread {
        private final AtomicLong timeout;
        private final GatewayUpdatedCallback callback;

        private ResumeTimer(final GatewayUpdatedCallback callback) {
            super("GatewayRestartManager:".concat(gatewayInstance));
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
                    GatewayUpdateManager.this.endUpdate(callback);
                    return;
                }
                final long timeout = this.timeout.decrementAndGet();
                //it is time to finalize update of the gateway
                if (timeout <= 0) {
                    GatewayUpdateManager.this.endUpdate(callback);
                    return;
                }
            }
        }

        private void reset() {
            timeout.set(GatewayUpdateManager.this.restartTimeout);
        }
    }

    private ResumeTimer timer;

    /**
     * Initializes a new restart manager.
     * @param gatewayInstance The name of the gateway instance.
     * @param delay The maximum time (in millis) used to await managed resource events.
     */
    public GatewayUpdateManager(final String gatewayInstance,
                                final long delay){
        this.restartTimeout = delay;
        this.timer = null;
        this.gatewayInstance = gatewayInstance;
    }

    /**
     * Determines whether the gateway instance is in updating state.
     * @return {@literal true}, if this gateway is in updating state.
     */
    public final boolean isUpdating(){
        return timer != null;
    }

    private synchronized void endUpdate(final GatewayUpdatedCallback callback){
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
     * Begin or prolong updating of the gateway's internal structure.
     * <p>
     *     It is recommended to call this method inside
     *     of {@link AbstractGateway#addFeature(String, javax.management.MBeanFeatureInfo)}
     *     or {@link AbstractGateway#removeFeature(String, javax.management.MBeanFeatureInfo)}
     *     method.
     * @param callback The callback used to notify about ending of the updating process.
     * @return {@literal true}, if this manager switches from active state to suspended; otherwise, {@literal false}.
     */
    final synchronized boolean beginUpdate(final GatewayUpdatedCallback callback){
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
     * Continues updating of the gateway.
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
    static GatewayUpdatedCallback combineCallbacks(final GatewayUpdatedCallback callback,
                                                   final GatewayUpdatedCallback... callbacks){
        return () -> {
            callback.updated();
            Arrays.stream(callbacks).forEach(GatewayUpdatedCallback::updated);
        };
    }
}
