package com.bytex.snamp.management.shell;

import com.bytex.snamp.concurrent.ThreadPoolConfig;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import org.apache.karaf.shell.commands.Command;

/**
 * Prints list of existing thread pools.
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "thread-pool-list",
        description = "List of SNAMP thread pools")
public final class ListOfThreadPoolsCommand extends AbstractThreadPoolCommand {

    private static void printThreadPoolConfig(final String name,
                                              final ThreadPoolConfig config,
                                              final StringBuilder output) {
        output
                .append(name).append(System.lineSeparator())
                .append(String.format("MinPoolSize: %s", config.getMinPoolSize())).append(System.lineSeparator())
                .append(String.format("MaxPoolSize: %s", config.getMaxPoolSize())).append(System.lineSeparator())
                .append(String.format("KeepAliveTime: %s", config.getKeepAliveTime())).append(System.lineSeparator())
                .append(String.format("Threads Priority: %s", config.getThreadPriority())).append(System.lineSeparator())
                .append(String.format("Queue Size: %s", config.isInfiniteQueue() ? "UNBOUNDED" : config.getQueueSize())).append(System.lineSeparator());
    }

    @Override
    void doExecute(final ThreadPoolRepository repository, final StringBuilder output) {
        printThreadPoolConfig(ThreadPoolRepository.DEFAULT_POOL, repository.getConfiguration(ThreadPoolRepository.DEFAULT_POOL), output);
        output.append(System.lineSeparator());
        for(final String name: repository) {
            printThreadPoolConfig(name, repository.getConfiguration(name), output);
            output.append(System.lineSeparator());
        }
    }
}
