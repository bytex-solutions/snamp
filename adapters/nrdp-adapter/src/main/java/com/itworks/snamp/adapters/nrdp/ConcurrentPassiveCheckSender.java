package com.itworks.snamp.adapters.nrdp;

import ch.shamu.jsendnrdp.NRDPException;
import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.impl.NagiosCheckSenderImpl;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ConcurrentPassiveCheckSender extends NagiosCheckSenderImpl implements Closeable {
    private final ExecutorService threadPool;

    ConcurrentPassiveCheckSender(final NRDPServerConnectionSettings settings,
                                 final Supplier<ExecutorService> threadPoolFactory) {
        super(settings);
        threadPool = threadPoolFactory.get();
    }

    public void send(final NagiosCheckResult checkResult){
        send(ImmutableList.of(checkResult));
    }

    @Override
    public void send(final Collection<NagiosCheckResult> results) {
        threadPool.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException, NRDPException {
                ConcurrentPassiveCheckSender.super.send(results);
                return null;
            }
        });
    }

    @Override
    public void close() {
        threadPool.shutdownNow();
    }
}
