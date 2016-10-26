package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

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

    @Override
    boolean doExecute(final EntityMap<? extends ThreadPoolConfiguration> repository, final StringBuilder output) {
        if (repository.remove(poolName) == null) {
            output.append("Thread pool doesn't exist").append(System.lineSeparator());
            return false;
        } else {
            output.append("Thread pool unregistered successfully").append(System.lineSeparator());
            return true;
        }
    }
}
