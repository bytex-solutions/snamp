package com.itworks.snamp.management.impl;

import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Created by temni on 2/8/2015.
 */
final class StartConnectorOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>>  {

    private static final String NAME = "startConnector";

    private static final String CONNECTOR_NAME_PARAM = "Name";

    StartConnectorOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, new OpenMBeanParameterInfoSupport(CONNECTOR_NAME_PARAM, "Connector name", SimpleType.STRING));
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM, String.class, arguments);
        ManagedResourceActivator.startResourceConnector(getBundleContextByObject(this), connectorName);
        return null;
    }
}
