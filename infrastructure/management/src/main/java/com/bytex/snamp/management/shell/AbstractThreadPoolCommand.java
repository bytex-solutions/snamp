package com.bytex.snamp.management.shell;

import com.bytex.snamp.configuration.ThreadPoolConfiguration;

/**
 * Provides abstract class for all thread pool management commands.
 * @author Roman Sakno
 * @since 1.2
 * @version 2.0
 */
abstract class AbstractThreadPoolCommand extends ConfigurationCommand<ThreadPoolConfiguration> {
    AbstractThreadPoolCommand(){
        super(ThreadPoolConfiguration.class);
    }
}
