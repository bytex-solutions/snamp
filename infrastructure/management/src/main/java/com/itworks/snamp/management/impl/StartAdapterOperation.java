package com.itworks.snamp.management.impl;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;


/**
 * The type Start adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StartAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final String NAME = "startAdapter";

    /**
     * Instantiates a new Start adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    StartAdapterOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, ADAPTER_NAME_PARAM);
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM.getName(), String.class, arguments);
        ResourceAdapterActivator.startResourceAdapter(getBundleContextByObject(this), adapterName);
        return null;
    }
}
