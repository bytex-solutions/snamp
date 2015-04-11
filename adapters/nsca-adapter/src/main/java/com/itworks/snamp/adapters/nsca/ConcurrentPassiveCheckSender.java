package com.itworks.snamp.adapters.nsca;

import com.google.common.base.Supplier;
import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.core.NagiosSettings;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ConcurrentPassiveCheckSender extends NagiosPassiveCheckSender implements Closeable {
   private final ExecutorService executor;

    ConcurrentPassiveCheckSender(final NagiosSettings settings,
                                 final Supplier<ExecutorService> threadPoolFactory) {
        super(settings);
        executor = threadPoolFactory.get();
    }

    @Override
    public void send(final MessagePayload payload) {
        executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ConcurrentPassiveCheckSender.super.send(payload);
                return null;
            }
        });
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
