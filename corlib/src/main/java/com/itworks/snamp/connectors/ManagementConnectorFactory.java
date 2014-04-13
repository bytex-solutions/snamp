package com.itworks.snamp.connectors;

import com.itworks.snamp.core.FrameworkService;

import java.util.Map;

/**
 * Represents management connector factory as OSGi service.
 * <p>
 *     This service should be exposed from {@link AbstractManagementConnectorBundleActivator} bundle activator.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface ManagementConnectorFactory extends FrameworkService {
    /**
     * The name of the management connector registration property that represents it name.
     */
    String CONNECTION_TYPE_PROPERTY = "connectionType";

    /**
     * The name of the management connector registration property that represents it connection string.
     */
    String CONNECTION_STRING_PROPERTY = "connectionString";

    /**
     * Creates a new instance of the connector and registers it in OSGi environment as a service.
     * @param connectionString The protocol-specific connection string.
     * @param connectionProperties The connection properties such as credentials.
     */
    void newInstance(final String connectionString, final Map<String, String> connectionProperties);

    /**
     * Determines whether the specified feature is supported.
     * @param feature Type of the feature to check, such as {@link NotificationSupport}.
     * @return {@literal true}, if the specified management connector feature is supported; otherwise, {@literal false}.
     * @see AttributeSupport
     * @see NotificationSupport
     */
    boolean supportsFeature(final Class<?> feature);
}
