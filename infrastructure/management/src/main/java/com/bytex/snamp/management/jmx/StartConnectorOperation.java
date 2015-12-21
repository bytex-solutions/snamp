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
 * The type Start connector operation.
 * @author Evgeniy Kirichenko
 */
final class StartConnectorOperation extends OpenMBean.OpenOperation<Void, SimpleType<Void>> implements CommonOpenTypesSupport<MBeanOperationInfo> {

    private static final String NAME = "startConnector";

    /**
     * Instantiates a new Start connector operation.
     *
     * @throws OpenDataException the open data exception
     */
    StartConnectorOperation() throws OpenDataException {
        super(NAME, SimpleType.VOID, CONNECTOR_NAME_PARAM);
    }

    @Override
    public Void invoke(Map<String, ?> arguments) throws BundleException {
        ManagedResourceActivator.startResourceConnector(getBundleContextOfObject(this), CONNECTOR_NAME_PARAM.getArgument(arguments));
        return null;
    }
}
