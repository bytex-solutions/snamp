package com.itworks.snamp.adapters;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractAdapter2 implements BundleActivator {

    /**
     * Starts the adapter.
     * @param context Adapter activation context.
     * @throws Exception Adapter could not be executed.
     */
    @Override
    public void start(final BundleContext context) throws Exception {

    }

    /**
     * Stops the adapter and releases all resources associated with it.
     * @param context Adapter deactivation context.
     * @throws Exception Adapter could not be stopped.
     */
    @Override
    public void stop(final BundleContext context) throws Exception {

    }
}
