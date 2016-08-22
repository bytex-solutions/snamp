package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.operations.OperationSupport;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@FunctionalInterface
interface OperationSupportProvider {
    OperationSupport getOperationSupport(final String connectorType);
}
