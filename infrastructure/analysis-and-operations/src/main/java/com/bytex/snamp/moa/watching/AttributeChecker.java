package com.bytex.snamp.moa.watching;

import com.bytex.snamp.connector.health.HealthCheckStatus;

import javax.management.Attribute;

/**
 * Converts attribute value into status of the component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface AttributeChecker {
    AttributeChecker OK = a -> HealthCheckStatus.OK;

    /**
     * Checks attribute value.
     * @param attribute Attribute value.
     * @return {@literal true}, if attribute satisfies to this condition; otherwise, {@literal false}.
     */
    HealthCheckStatus getStatus(final Attribute attribute);
}
