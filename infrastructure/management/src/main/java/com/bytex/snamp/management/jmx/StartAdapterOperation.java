package com.bytex.snamp.management.jmx;

import com.bytex.snamp.gateway.GatewayActivator;
import org.osgi.framework.BundleException;

import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * The type Start adapter operation.
 * @author Evgeniy Kirichenko
 */
final class StartAdapterOperation extends AbstractAdapterOperation {

    private static final String NAME = "startAdapter";

    /**
     * Instantiates a new Start adapter operation.
     *
     * @throws OpenDataException the open data exception
     */
    StartAdapterOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String adapterInstance) throws BundleException {
        GatewayActivator.enableGateway(getBundleContextOfObject(this), adapterInstance);
    }
}
