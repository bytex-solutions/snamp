package com.bytex.snamp.management.shell;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.core.ServiceHolder;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * Provides abstract class for all thread pool management commands.
 * @author Roman Sakno
 * @since 1.2
 * @version 1.2
 */
abstract class AbstractThreadPoolCommand extends OsgiCommandSupport implements SnampShellCommand {
    abstract void doExecute(final ThreadPoolRepository repository, final StringBuilder output);

    @Override
    protected final Object doExecute() {
        final ServiceHolder<ThreadPoolRepository> repository = ServiceHolder.tryCreate(bundleContext, ThreadPoolRepository.class);
        if(repository != null)
            try{
                final StringBuilder output = new StringBuilder();
                doExecute(repository.get(), output);
                return output;
            }
            finally {
                repository.release(bundleContext);
            }
        else return "No thread pool management service";
    }
}
