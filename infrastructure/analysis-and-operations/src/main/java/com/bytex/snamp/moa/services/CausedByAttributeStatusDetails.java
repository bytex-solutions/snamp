package com.bytex.snamp.moa.services;

import com.bytex.snamp.health.HealthStatus;
import com.bytex.snamp.health.CausedByAttribute;

import javax.management.Attribute;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CausedByAttributeStatusDetails extends TypedStatusDetails<CausedByAttribute> {
    private final HealthStatus status;

    CausedByAttributeStatusDetails(final String resourceName,
                                   final Attribute attribute,
                                   final HealthStatus status){
        super(resourceName, new CausedByAttribute(attribute));
        this.status = Objects.requireNonNull(status);
    }

    @Override
    public HealthStatus getStatus() {
        return status;
    }
}
