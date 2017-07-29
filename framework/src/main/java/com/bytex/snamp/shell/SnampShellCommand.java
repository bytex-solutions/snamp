package com.bytex.snamp.shell;

import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class SnampShellCommand {
    /**
     * Namespace of commands.
     */
    public static final String SCOPE = "snamp";

    protected final BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    protected abstract void execute(final PrintWriter writer) throws Exception;

    public final CharSequence execute() throws Exception {
        try (final StringWriter output = new StringWriter(); final PrintWriter writer = new PrintWriter(output, false)) {
            execute(writer);
            writer.flush();
            return output.getBuffer();
        }
    }

    protected static void checkInterrupted() throws InterruptedException {
        Thread.yield();
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    }
}
