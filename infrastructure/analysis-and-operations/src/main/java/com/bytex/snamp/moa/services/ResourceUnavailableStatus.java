package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.supervision.HealthStatusCore;
import com.bytex.snamp.connector.supervision.ResourceInGroupIsNotUnavailable;

import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ResourceUnavailableStatus extends TypedStatusDetails<ResourceInGroupIsNotUnavailable> {
    ResourceUnavailableStatus(final String resourceName, final JMException error){
        super(resourceName, new ResourceInGroupIsNotUnavailable(error));
    }

    @Override
    public HealthStatusCore getStatus() {
        return HealthStatusCore.MALFUNCTION;
    }
}
