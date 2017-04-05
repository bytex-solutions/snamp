package com.bytex.snamp.connector.attributes.checkers;

import javax.management.Attribute;

/**
 * Represents functional interface used to calculate attribute check status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
public interface AttributeChecker {
    AttributeCheckStatus getStatus(final Attribute attribute);
}
