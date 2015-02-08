package com.itworks.snamp.management.impl;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.management.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Created by temni on 2/8/2015.
 */
final class StopAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>>  {

    private static final String NAME = "stopAdapter";

    private static final String ADAPTER_NAME_PARAM = "Name";

    StopAdapterOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, new OpenMBeanParameterInfoSupport(ADAPTER_NAME_PARAM, "Adapter name", SimpleType.STRING));
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM, String.class, arguments);
        ResourceAdapterActivator.stopResourceAdapter(getBundleContextByObject(this), adapterName);
        return null;
    }
}
