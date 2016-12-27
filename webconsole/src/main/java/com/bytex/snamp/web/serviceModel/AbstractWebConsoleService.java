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
    private final WeakEventListenerList<WebEventListener, WebEvent> listeners = WeakEventListenerList.create(WebEventListener::accept);

    @Override
    public final void addWebEventListener(final WebEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public final void removeWebEventListener(final WebEventListener listener) {
        listeners.remove(listener);
    }

    protected final void fireWebEvent(final WebEvent event){
        listeners.fire(event);
    }

    final void fireWebEvent(final Consumer<? super WebEventListener> listenerInvoker){
        listeners.forEach(listenerInvoker);
    }

    final void fireWebEvent(final Consumer<? super WebEventListener> listenerInvoker, final Executor executor) {
        listeners.forEach(listener -> executor.execute(() -> listenerInvoker.accept(listener)));
    }

    @Override
    public void close() throws Exception {
        listeners.clear();
    }
}
