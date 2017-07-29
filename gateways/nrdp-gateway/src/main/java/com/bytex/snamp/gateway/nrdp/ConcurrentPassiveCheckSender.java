package com.bytex.snamp.gateway.nrdp;

import ch.shamu.jsendnrdp.NRDPServerConnectionSettings;
import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.impl.NagiosCheckSenderImpl;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class ConcurrentPassiveCheckSender extends NagiosCheckSenderImpl {
    private final ExecutorService threadPool;

    ConcurrentPassiveCheckSender(final NRDPServerConnectionSettings settings,
                                 final ExecutorService threadPool) {
        super(settings);
        this.threadPool = threadPool;
    }

    void send(final NagiosCheckResult checkResult){
        send(ImmutableList.of(checkResult));
    }

    <I> void send(final Function<? super I, NagiosCheckResult> checkResult, final I input) {
        threadPool.submit(() -> {
            final NagiosCheckResult result = checkResult.apply(input);
            if (result != null)
                ConcurrentPassiveCheckSender.super.send(ImmutableList.of(result));
            return result;
        });
    }

    @Override
    public void send(final Collection<NagiosCheckResult> results) {
        threadPool.submit(() -> {
            ConcurrentPassiveCheckSender.super.send(results);
            return null;
        });
    }
}
