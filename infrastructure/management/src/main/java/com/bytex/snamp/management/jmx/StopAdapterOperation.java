package com.bytex.snamp.management.jmx;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

/**
 * The type Stop adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StopAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> implements CommonOpenTypesSupport<MBeanOperationInfo> {

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
