package com.bytex.snamp.core;

import com.bytex.snamp.Convert;
import com.bytex.snamp.SafeCloseable;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.bytex.snamp.core.SharedObjectType.COMMUNICATOR;

/**
 * Exchange point based on communicator.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class Exchange<T extends Serializable> implements Consumer<T>, SafeCloseable {
    private static final class WeakReceiver<T extends Serializable> extends WeakReference<Exchange<T>> implements Communicator.MessageListener{
        private final Class<T> payloadType;

        private WeakReceiver(final Exchange<T> exchange,
                             final Class<T> payloadType) {
            super(exchange);
            this.payloadType = Objects.requireNonNull(payloadType);
        }
        
        @Override
        public void accept(final Communicator.MessageEvent incomingMessage) {
            final Exchange<T> exchange = get();
            
            if (exchange != null)
                Convert.toType(incomingMessage.getPayload(), payloadType).ifPresent(exchange);
        }
    }

    private final SafeCloseable subscription;
    private final Communicator communicator;

    protected Exchange(final Communicator communicator,
                       final Predicate<? super Communicator.MessageEvent> messageFilter,
                       final Class<T> payloadType) {
        this.communicator = Objects.requireNonNull(communicator);
        subscription = communicator.addMessageListener(new WeakReceiver<>(this, payloadType), messageFilter);
    }

    protected Exchange(final ClusterMember clusterMember,
                     final String channelName,
                     final Predicate<? super Communicator.MessageEvent> messageFilter,
                     final Class<T> payloadType) {
        this(clusterMember.getService(channelName, COMMUNICATOR)
                .orElseThrow(AssertionError::new), messageFilter, payloadType);
    }

    @Override
    public abstract void accept(final T payload);

    /**
     * Sends serializable object.
     * @param payload An object to be sent.
     */
    public void send(final T payload) {
        communicator.sendSignal(payload);
    }

    /**
     * Releases all resources associated with this object.
     */
    @Override
    public void close() {
        subscription.close();
    }
}
