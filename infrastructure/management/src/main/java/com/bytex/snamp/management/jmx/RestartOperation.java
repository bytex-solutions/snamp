package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * Restart all SNAMP components inside of Karaf container.
 * @author Evgeniy Kirichenko
 */
final class RestartOperation extends OpenMBean.OpenOneWayOperation {

    private static final String NAME = "restart";

    /**
     * Instantiates a new Restart operation.
     */
    RestartOperation() {
        super(NAME);
    }

    @Override
    public void invoke() throws Exception {
        SnampManagerImpl.restart(getBundleContextOfObject(this));
    }
}
