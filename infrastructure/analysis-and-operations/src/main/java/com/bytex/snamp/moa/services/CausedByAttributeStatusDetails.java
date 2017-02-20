package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.health.HealthCheckStatus;
import com.bytex.snamp.moa.watching.CausedByAttribute;

import javax.management.Attribute;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CausedByAttributeStatusDetails extends TypedStatusDetails<CausedByAttribute> {
    private final HealthCheckStatus status;

    CausedByAttributeStatusDetails(final String resourceName,
                                   final Attribute attribute,
                                   final HealthCheckStatus status){
        super(resourceName, new CausedByAttribute(attribute));
        this.status = Objects.requireNonNull(status);
    }

    @Override
    public HealthCheckStatus getStatus() {
        return status;
    }
}
