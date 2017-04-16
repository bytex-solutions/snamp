package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.operations.OperationSupport;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface OperationSupportProvider {
    Optional<OperationSupport> getOperationSupport(final String connectorType);
}
