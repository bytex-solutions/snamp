package com.bytex.snamp.management.shell;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import java.time.Duration;

import static com.bytex.snamp.configuration.ThreadPoolConfiguration.*;

/**
 * Registers a new thread pool in globally accessible repository.
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "thread-pool-add",
        description = "Register a new thread pool")
@Service
public final class AddThreadPoolCommand extends AbstractThreadPoolCommand {
    @Argument(index = 0, name = "name", required = true, description = "Name of the thread pool to register")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private String poolName = "";

    @Option(name = "-m", aliases = "--minPoolSize", description = "A number of threads to keep in the pool")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private int minPoolSize = DEFAULT_MIN_POOL_SIZE;

    @Option(name = "-M", aliases = "--maxPoolSize", description = "Maximum number of threads to allow in the pool")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private int maxPoolSize = DEFAULT_MAX_POOL_SIZE;

    @Option(name = "-p", aliases = "--priority", description = "Priority of thread in the pool: 1-10")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private int threadPriority = DEFAULT_THREAD_PRIORITY;

    @Option(name = "-t", aliases = "--keepAliveTime", description = "When the number of threads is greater than the minimum, this is the maximum time (in millis) that excess idle threads\n" +
            " will wait for new tasks before terminating")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private long keepAliveTime = DEFAULT_KEEP_ALIVE_TIME.toMillis();

    @Option(name = "-q", aliases = "--queueSize", description = "Maximum number of scheduled tasks waiting in the queue. -1 for infinite queue")
    @SpecialUse(SpecialUse.Case.REFLECTION)
    private int queueSize = INFINITE_QUEUE_SIZE;

    @Override
    boolean doExecute(final EntityMap<? extends ThreadPoolConfiguration> configuration, final StringBuilder output) {
        if (configuration.containsKey(poolName)) {
            output.append("Thread pool with the specified name is already registered");
            return false;
        }
        configuration.addAndConsume(poolName, config -> {
            config.setMinPoolSize(minPoolSize);
            config.setMaxPoolSize(maxPoolSize);
            config.setThreadPriority(threadPriority);
            if (queueSize < 0)
                config.setQueueSize(INFINITE_QUEUE_SIZE);
            else
                config.setQueueSize(queueSize);
            config.setKeepAliveTime(Duration.ofMillis(keepAliveTime));
        });
        output.append("Thread pool is registered successfully");
        return true;
    }
}
