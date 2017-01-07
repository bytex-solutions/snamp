package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.WeakEventListenerList;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractWebConsoleService implements WebConsoleService {
    private final WeakEventListenerList<WebConsoleSession, WebMessage> listeners = WeakEventListenerList.create(WebConsoleSession::sendMessage);

    @Override
    public final void attachSession(final WebConsoleSession listener) {
        listeners.add(listener);
    }

    protected final void sendBroadcastMessage(final WebMessage message){
        listeners.fire(message);
    }

    protected final void forEachSession(final Consumer<? super WebConsoleSession> sessionConsumer, final Executor executor) {
        listeners.parallelForEach(sessionConsumer, executor);
    }

    @Override
    public void close() throws Exception {
        listeners.clear();
    }
}
