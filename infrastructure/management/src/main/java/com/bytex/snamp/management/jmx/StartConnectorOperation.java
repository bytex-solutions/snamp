package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.ManagedResourceActivator;
import org.osgi.framework.BundleException;

import javax.management.openmbean.OpenDataException;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;


/**
 * The type Start connector operation.
 * @author Evgeniy Kirichenko
 */
final class StartConnectorOperation extends AbstractConnectorOperation {

    private static final String NAME = "startConnector";

    /**
     * Instantiates a new Start connector operation.
     *
     * @throws OpenDataException the open data exception
     */
    StartConnectorOperation() throws OpenDataException {
        super(NAME);
    }

    @Override
    void invoke(final String resourceName) throws BundleException {
        ManagedResourceActivator.enableConnector(getBundleContextOfObject(this), resourceName);
    }
}
