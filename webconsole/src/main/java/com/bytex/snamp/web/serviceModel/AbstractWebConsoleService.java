package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.WeakEventListenerList;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractWebConsoleService implements WebConsoleService {
    private final WeakEventListenerList<WebConsoleSession, WebMessage> listeners = WeakEventListenerList.create(WebConsoleSession::sendMessage);
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public final void attachSession(final WebConsoleSession listener) {
        listeners.add(listener);
        if(initialized.compareAndSet(false, true))
            initialize();
    }

    protected final void sendBroadcastMessage(final WebMessage message){
        listeners.fire(message);
    }

    protected final void forEachSession(final Consumer<? super WebConsoleSession> sessionConsumer) {
        listeners.forEach(sessionConsumer);
    }

    protected final void forEachSession(final Consumer<? super WebConsoleSession> sessionConsumer, final Executor executor) {
        listeners.parallelForEach(sessionConsumer, executor);
    }
    /**
     * Initializes this service.
     * <p />
     * Services for SNAMP Web Console has lazy initialization. They will be initialized when the first session of the client
     * will be attached. This approach helps to save computation resources when SNAMP deployed as cluster with many nodes.
     */
    protected abstract void initialize();

    protected final boolean isInitialized(){
        return initialized.get();
    }

    @Override
    public void close() throws Exception {
        listeners.clear();
    }
}
