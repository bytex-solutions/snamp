package com.bytex.snamp.adapters.syslog;

import com.bytex.snamp.SafeCloseable;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.AbstractSyslogMessageSender;
import com.cloudbees.syslog.sender.SyslogMessageSender;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ConcurrentSyslogMessageSender extends AbstractSyslogMessageSender implements Closeable, SafeCloseable {
    private final SyslogMessageSender messageSender;
    private final ExecutorService executor;

    ConcurrentSyslogMessageSender(final SyslogMessageSender sequentialSender){
        messageSender = Objects.requireNonNull(sequentialSender);
        executor = Executors.newSingleThreadExecutor();
    }

    private static void sendMessage(final SyslogMessageSender sender,
                                    final ExecutorService executor,
                                    final SyslogMessage message) {
        executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException {
                sender.sendMessage(message);
                return null;
            }
        });
    }

    @Override
    public void sendMessage(@SuppressWarnings("NullableProblems") final SyslogMessage message) {
        sendMessage(messageSender, executor, message);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
