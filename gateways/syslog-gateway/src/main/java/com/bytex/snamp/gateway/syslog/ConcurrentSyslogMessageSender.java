package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.SafeCloseable;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.AbstractSyslogMessageSender;
import com.cloudbees.syslog.sender.SyslogMessageSender;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class ConcurrentSyslogMessageSender extends AbstractSyslogMessageSender implements Closeable, SafeCloseable {
    private final SyslogMessageSender messageSender;
    private final ExecutorService executor;

    ConcurrentSyslogMessageSender(final SyslogMessageSender sequentialSender,
                                  final ExecutorService threadPool){
        messageSender = Objects.requireNonNull(sequentialSender);
        executor = Objects.requireNonNull(threadPool);
    }

    private static void sendMessage(final SyslogMessageSender sender,
                                    final ExecutorService executor,
                                    final SyslogMessage message) {
        executor.submit(() -> {
            sender.sendMessage(message);
            return null;
        });
    }

    @Override
    public void sendMessage(@Nonnull final SyslogMessage message) {
        sendMessage(messageSender, executor, message);
    }

    @Override
    public void close() {

    }
}
