package com.bytex.snamp.gateway.nsca;

import com.googlecode.jsendnsca.core.MessagePayload;
import com.googlecode.jsendnsca.core.NagiosException;
import com.googlecode.jsendnsca.core.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.core.NagiosSettings;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ConcurrentPassiveCheckSender extends NagiosPassiveCheckSender {
   private final ExecutorService executor;

    ConcurrentPassiveCheckSender(final NagiosSettings settings,
                                 final ExecutorService threadPool) {
        super(settings);
        this.executor = threadPool;
    }

    <I> void send(final Function<? super I, MessagePayload> payload, final I input) {
        executor.submit(() -> {
            final MessagePayload result = payload.apply(input);
            if (result != null)
                ConcurrentPassiveCheckSender.super.send(result);
            return result;
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
}
