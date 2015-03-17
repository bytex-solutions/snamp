package com.itworks.snamp.management.impl;

import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;


/**
 * The type Stop connector operation.
 * @author Evgeniy Kirichenko
 */
final class StopConnectorOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>>  {

    private static final String NAME = "stopConnector";

    private static final String CONNECTOR_NAME_PARAM = "Name";

    /**
     * Instantiates a new Stop connector operation.
     *
     * @throws OpenDataException the open data exception
     */
    StopConnectorOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, new OpenMBeanParameterInfoSupport(CONNECTOR_NAME_PARAM, "Connector name", SimpleType.STRING));
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM, String.class, arguments);
        ManagedResourceActivator.stopResourceConnector(getBundleContextByObject(this), connectorName);
        return null;
    }
}
