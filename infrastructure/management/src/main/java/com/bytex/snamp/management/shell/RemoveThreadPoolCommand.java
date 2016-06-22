package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

/**
 * Removes thread pool.
 */
@Command(scope = SnampShellCommand.SCOPE,
    name = "thread-pool-remove",
    description = "Remove thread pool and, optionally, terminate all scheduled tasks")
public final class RemoveThreadPoolCommand extends AbstractThreadPoolCommand {
    @Argument(index = 0, name = "name", required = true, description = "Name of thread pool to remove")
    @SpecialUse
    private String poolName = "";

    @Option(name = "-t", aliases = {"--terminate"}, required = false, description = "Terminate all scheduled tasks in the thread pool")
    @SpecialUse
    private boolean terminateTasks = false;

    @Override
    void doExecute(final ThreadPoolRepository repository, final StringBuilder output) {
        if (repository.unregisterThreadPool(poolName, terminateTasks))
            output.append("Thread pool unregistered successfully").append(System.lineSeparator());
        else
            output.append("Thread pool doesn't exist").append(System.lineSeparator());
    }
}
