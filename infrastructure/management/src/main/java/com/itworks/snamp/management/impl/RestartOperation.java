package com.itworks.snamp.management.impl;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.management.jmx.OpenMBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Created by temni on 2/8/2015.
 */
final class RestartOperation extends OpenMBean.OpenOneWayOperation {

    private static final String NAME = "restart";

    RestartOperation() {
        super(NAME);
    }

    private static void restart(final BundleContext context) throws BundleException {
        //first, stop all adapters
        ResourceAdapterActivator.stopResourceAdapters(context);
        //second, stop all connectors
        ManagedResourceActivator.stopResourceConnectors(context);
        //third, start all connectors
        ManagedResourceActivator.startResourceConnectors(context);
        //fourth, start all adapters
        ResourceAdapterActivator.startResourceAdapters(context);
    }

    @Override
    public void invoke() throws Exception {
        restart(getBundleContextByObject(this));
    }
}
