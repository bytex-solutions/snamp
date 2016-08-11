package com.bytex.snamp.management.jmx;

import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.framework.BundleException;

import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * The type Start adapter operation.
 * @author Evgeniy Kirichenko
 */
final class EnableGatewayOperation extends AbstractGatewayOperation {

    private static final String NAME = "enableGateway";

    /**
     * Instantiates a new Start adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    EnableGatewayOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String gatewayType) throws BundleException {
        GatewayActivator.enableGateway(getBundleContextOfObject(this), gatewayType);
    }
}
