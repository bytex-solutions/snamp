package com.bytex.snamp.management.jmx;

import com.bytex.snamp.gateway.GatewayActivator;

import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Implementation of JMX operation called {@code disableGateway}.
 * @author Evgeniy Kirichenko
 */
final class DisableGatewayOperation extends AbstractGatewayOperation {

    private static final String NAME = "disableGateway";

    DisableGatewayOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String gatewayType) throws Exception {
        GatewayActivator.disableGateway(getBundleContextOfObject(this), gatewayType);
    }
}
