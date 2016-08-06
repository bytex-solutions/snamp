package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import org.apache.karaf.shell.commands.Command;

/**
 * Prints list of existing thread pools.
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "thread-pool-list",
        description = "List of SNAMP thread pools")
public final class ListOfThreadPoolsCommand extends AbstractThreadPoolCommand {

    private static void printThreadPoolConfig(final String name,
                                              final ThreadPoolConfiguration config,
                                              final StringBuilder output) {
        output
                .append(name).append(System.lineSeparator())
                .append(String.format("MinPoolSize: %s", config.getMinPoolSize())).append(System.lineSeparator())
                .append(String.format("MaxPoolSize: %s", config.getMaxPoolSize())).append(System.lineSeparator())
                .append(String.format("KeepAliveTime: %s", config.getKeepAliveTime())).append(System.lineSeparator())
                .append(String.format("Threads Priority: %s", config.getThreadPriority())).append(System.lineSeparator())
                .append(String.format("Queue Size: %s", config.getQueueSize() == ThreadPoolConfiguration.INFINITE_QUEUE_SIZE ? "UNBOUNDED" : config.getQueueSize())).append(System.lineSeparator());
    }

    @Override
    boolean doExecute(final EntityMap<? extends ThreadPoolConfiguration> configuration, final StringBuilder output) {
        output.append(System.lineSeparator());
        configuration.entrySet().forEach(entry -> {
            printThreadPoolConfig(entry.getKey(), entry.getValue(), output);
            output.append(System.lineSeparator());
        });
        return false;
    }
}
