package com.itworks.snamp.management.impl;

import com.itworks.snamp.adapters.ResourceAdapterActivator;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;


/**
 * The type Start adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StartAdapterOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>>  {

    private static final String NAME = "startAdapter";

    private static final String ADAPTER_NAME_PARAM = "Name";

    /**
     * Instantiates a new Start adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    StartAdapterOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, new OpenMBeanParameterInfoSupport(ADAPTER_NAME_PARAM, "Adapter name", SimpleType.STRING));
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM, String.class, arguments);
        ResourceAdapterActivator.startResourceAdapter(getBundleContextByObject(this), adapterName);
        return null;
    }
}
