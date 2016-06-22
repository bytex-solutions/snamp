package com.bytex.snamp.management.shell;

import com.bytex.snamp.concurrent.ThreadPoolConfig;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;

/**
 * Registers a new thread pool in globally accessible repository.
 */
@Command(scope = SnampShellCommand.SCOPE,
        name = "thread-pool-add",
        description = "Register a new thread pool")
public final class AddThreadPoolCommand extends AbstractThreadPoolCommand {
    @Argument(index = 0, name = "name", required = true, description = "Name of the thread pool to register")
    private String poolName = "";

    @Option(name = "-m", aliases = "--minPoolSize", description = "A number of threads to keep in the pool")
    private int minPoolSize = ThreadPoolConfig.DEFAULT_MIN_POOL_SIZE;

    @Option(name = "-M", aliases = "--maxPoolSize", description = "Maximum number of threads to allow in the pool")
    private int maxPoolSize = ThreadPoolConfig.DEFAULT_MAX_POOL_SIZE;

    @Option(name = "-p", aliases = "--priority", description = "Priority of thread in the pool: 1-10")
    private int threadPriority = ThreadPoolConfig.DEFAULT_PRIORITY;

    @Option(name = "-t", aliases = "--keepAliveTime", description = "When the number of threads is greater than the minimum, this is the maximum time (in millis) that excess idle threads\n" +
            " will wait for new tasks before terminating")
    private long keepAliveTime = ThreadPoolConfig.DEFAULT_KEEP_ALIVE_TIME.toMillis();

    @Option(name = "-q", aliases = "--queueSize", description = "Maximum number of scheduled tasks waiting in the queue. -1 for infinite queue")
    private int queueSize = ThreadPoolConfig.INFINITE_QUEUE_SIZE;

    @Override
    void doExecute(final ThreadPoolRepository repository, final StringBuilder output) {
        if(repository.getConfiguration(poolName) != null){
            output.append("Thread pool with the specified name is already registered");
            return;
        }
        final ThreadPoolConfig config = new ThreadPoolConfig();
        config.setMinPoolSize(minPoolSize);
        config.setMaxPoolSize(maxPoolSize);
        config.setThreadPriority(threadPriority);
        if(queueSize < 0)
            config.useInifiniteQueue();
        else
            config.setQueueSize(queueSize);
        config.setKeepAliveTime(keepAliveTime);
        repository.registerThreadPool(poolName, config);
        output.append("Thread pool is registered successfully");
    }
}
