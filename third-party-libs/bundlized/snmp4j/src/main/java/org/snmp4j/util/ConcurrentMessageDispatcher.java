package org.snmp4j.util;

import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.TransportMapping;
import org.snmp4j.TransportStateReference;
import org.snmp4j.smi.Address;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Represents SNMP message dispatcher that supports thread pool for
 * concurrent message processing. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ConcurrentMessageDispatcher extends MessageDispatcherImpl {
    private final ExecutorService executor;

    public ConcurrentMessageDispatcher(final ExecutorService threadPool) {
        this.executor = Objects.requireNonNull(threadPool);
    }

    @Override
    public void processMessage(final TransportMapping sourceTransport, final Address incomingAddress, final ByteBuffer wholeMessage, final TransportStateReference tmStateReference) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                ConcurrentMessageDispatcher.super.processMessage(sourceTransport,
                        incomingAddress,
                        wholeMessage,
                        tmStateReference);
            }
        });
    }
}
