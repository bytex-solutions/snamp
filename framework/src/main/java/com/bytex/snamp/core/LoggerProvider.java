package com.bytex.snamp.core;

import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import java.util.logging.Logger;

/**
 * Represents OSGi-compliant logger manager.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class LoggerProvider {

    private LoggerProvider(){
    }

    /**
     * Gets logger associated with the specified bundle context.
     * @param context The bundle context. May be {@literal null}.
     * @return The logger associated with the bundle.
     */
    public static Logger getLoggerForBundle(final BundleContext context) {
        return context == null ? Logger.getAnonymousLogger() : Logger.getLogger(context.getBundle().getSymbolicName());
    }

    public static Logger getLoggerForObject(final Object requester){
        return getLoggerForBundle(Utils.getBundleContextOfObject(requester));
    }
}
