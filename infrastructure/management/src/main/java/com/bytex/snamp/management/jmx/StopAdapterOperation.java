package com.bytex.snamp.management.jmx;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * The type Stop adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StopAdapterOperation extends AbstractAdapterOperation {

    private static final String NAME = "stopAdapter";

    /**
     * Instantiates a new Stop adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    StopAdapterOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String adapterInstance) throws Exception {
        ResourceAdapterActivator.stopResourceAdapter(getBundleContextOfObject(this), adapterInstance);
    }
}
