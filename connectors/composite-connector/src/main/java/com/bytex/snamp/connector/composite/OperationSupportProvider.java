package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.operations.OperationManager;

import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@FunctionalInterface
interface OperationSupportProvider {
    Optional<OperationManager> getOperationSupport(final String connectorType);
}
