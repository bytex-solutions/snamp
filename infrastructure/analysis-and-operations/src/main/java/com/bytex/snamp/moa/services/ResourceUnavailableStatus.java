package com.bytex.snamp.moa.services;

import com.bytex.snamp.health.HealthStatus;
import com.bytex.snamp.health.ResourceUnavailable;

import javax.management.JMException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ResourceUnavailableStatus extends TypedStatusDetails<ResourceUnavailable> {
    ResourceUnavailableStatus(final String resourceName, final JMException error){
        super(resourceName, new ResourceUnavailable(error));
    }

    @Override
    public HealthStatus getStatus() {
        return HealthStatus.MALFUNCTION;
    }
}
