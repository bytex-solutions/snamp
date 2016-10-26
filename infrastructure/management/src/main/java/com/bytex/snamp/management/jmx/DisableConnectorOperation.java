package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.framework.BundleException;

import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * The type Stop connector operation.
 * @author Evgeniy Kirichenko
 */
final class DisableConnectorOperation extends AbstractConnectorOperation{

    private static final String NAME = "disableConnector";

    /**
     * Instantiates a new Stop connector operation.
     *
     * @throws OpenDataException the open data exception
     */
    DisableConnectorOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String resourceName) throws BundleException {
        ManagedResourceActivator.disableConnector(getBundleContextOfObject(this), resourceName);
    }
}
