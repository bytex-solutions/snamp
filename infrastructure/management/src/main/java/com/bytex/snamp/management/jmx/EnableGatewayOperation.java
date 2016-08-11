package com.bytex.snamp.management.jmx;

import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.framework.BundleException;

import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * Implementation of JMX operation called {@code disableGateway}.
 * @author Evgeniy Kirichenko
 */
final class EnableGatewayOperation extends AbstractGatewayOperation {

    private static final String NAME = "enableGateway";

    EnableGatewayOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String gatewayType) throws BundleException {
        GatewayActivator.enableGateway(getBundleContextOfObject(this), gatewayType);
    }
}
