package com.itworks.snamp.management.impl;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * The type Stop adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StopAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> implements CommonOpenTypesSupport {

    private static final String NAME = "stopAdapter";

    /**
     * Instantiates a new Stop adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    StopAdapterOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, ADAPTER_NAME_PARAM);
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM.getName(), String.class, arguments);
        ResourceAdapterActivator.stopResourceAdapter(getBundleContextByObject(this), adapterName);
        return null;
    }
}
