package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.AbstractWeakEventListenerList;
import com.bytex.snamp.core.LoggerProvider;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.osgi.framework.BundleContext;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractWebConsoleService implements WebConsoleService {
    private final AbstractWeakEventListenerList<WebConsoleSession, WebMessage> listeners = AbstractWeakEventListenerList.create(WebConsoleSession::sendMessage);
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    protected class WebConsoleServiceMessage extends WebMessage {
        private static final long serialVersionUID = 7176815454191529198L;

        protected WebConsoleServiceMessage() {
            super(AbstractWebConsoleService.this);
        }

        /**
         * The object on which the Event initially occurred.
         *
         * @return The object on which the Event initially occurred.
         */
        @Override
        @JsonIgnore
        public WebConsoleService getSource() {
            return AbstractWebConsoleService.this;
        }
    }

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

    protected static <S extends AbstractWebConsoleService> void forEachSession(final S service,
                                                                              final BiConsumer<? super S, ? super WebConsoleSession> sessionHandler,
                                                                              final Executor executor) {
        final class WebConsoleSessionTask extends WeakReference<S> implements Consumer<WebConsoleSession> {
            private WebConsoleSessionTask() {
                super(service);
            }

            @Override
            public void accept(final WebConsoleSession session) {
                final S service = get();
                if (service != null)
                    sessionHandler.accept(service, session);
                clear();
            }
        }
        service.forEachSession(new WebConsoleSessionTask(), executor);
    }

    protected final Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    protected final BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
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
