package com.bytex.snamp.moa.services;

import com.bytex.snamp.connector.supervision.HealthStatusCore;
import com.bytex.snamp.connector.supervision.InvalidAttributeValue;

import javax.management.Attribute;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class CausedByAttributeStatusDetails extends TypedStatusDetails<InvalidAttributeValue> {
    private final HealthStatusCore status;

    CausedByAttributeStatusDetails(final String resourceName,
                                   final Attribute attribute,
                                   final HealthStatusCore status){
        super(resourceName, new InvalidAttributeValue(attribute));
        this.status = Objects.requireNonNull(status);
    }

    @Override
    public HealthStatusCore getStatus() {
        return status;
    }
}
