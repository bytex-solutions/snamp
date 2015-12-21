package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connectors.ManagedResourceActivator;
import com.bytex.snamp.jmx.OpenMBean;
import org.osgi.framework.BundleException;

import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * The type Stop connector operation.
 * @author Evgeniy Kirichenko
 */
final class StopConnectorOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final String NAME = "stopConnector";

    /**
     * Instantiates a new Stop connector operation.
     *
     * @throws OpenDataException the open data exception
     */
    StopConnectorOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, CONNECTOR_NAME_PARAM);
    }

    @Override
    public Void invoke(final Map<String, ?> arguments) throws BundleException {
        ManagedResourceActivator.stopResourceConnector(getBundleContextOfObject(this),
                CONNECTOR_NAME_PARAM.getArgument(arguments));
        return null;
    }
}
