package com.bytex.snamp.adapters.nsca;

import com.bytex.snamp.SafeCloseable;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.NagiosException;
import com.googlecode.jsendnsca.core.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.core.NagiosSettings;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ConcurrentPassiveCheckSender extends NagiosPassiveCheckSender implements Closeable, SafeCloseable {
   private final ExecutorService executor;

    ConcurrentPassiveCheckSender(final NagiosSettings settings,
                                 final Supplier<ExecutorService> threadPoolFactory) {
        super(settings);
        executor = threadPoolFactory.get();
    }

    <I> void send(final Function<? super I, MessagePayload> payload, final I input) {
        executor.submit(new Callable<MessagePayload>() {
            @Override
            public MessagePayload call() throws IOException, NagiosException {
                final MessagePayload result = payload.apply(input);
                if (result != null)
                    ConcurrentPassiveCheckSender.super.send(result);
                return result;
            }
        });
    }

    @Override
    public void send(final MessagePayload payload) {
        executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException, NagiosException {
                ConcurrentPassiveCheckSender.super.send(payload);
                return null;
            }
        });
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
