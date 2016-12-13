package com.bytex.snamp.management.shell;

import com.bytex.snamp.internal.Utils;
import org.apache.karaf.shell.api.action.Action;
import org.osgi.framework.BundleContext;

/**
 * Represents Karaf shell command for manipulating by SNAMP components.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class SnampShellCommand implements Action {
    /**
     * Namespace of commands.
     */
    static final String SCOPE = "snamp";

    final BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    @Override
    public abstract Object execute() throws Exception;

    static void checkInterrupted() throws InterruptedException {
        Thread.yield();
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }
}
