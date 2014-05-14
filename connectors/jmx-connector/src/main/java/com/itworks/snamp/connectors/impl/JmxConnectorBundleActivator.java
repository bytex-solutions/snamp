package com.itworks.snamp.connectors.impl;

import com.itworks.snamp.connectors.AbstractManagementConnectorBundleActivator;

import java.net.MalformedURLException;
import java.util.Map;


/**
 * Represents bundle activator for JMX connector.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("UnusedDeclaration")
public final class JmxConnectorBundleActivator extends AbstractManagementConnectorBundleActivator<JmxConnector> {

    private static final class JmxConnectorProvider extends NotificationSupportProvider<JmxConnector>{

        public JmxConnectorProvider(final String targetName){
            super(targetName, JmxConnectorLimitations.licenseReader);
        }

        /**
         * Creates a new instance of the management connector that supports notifications.
         *
         * @param connectionString  The connection string.
         * @param connectionOptions The connection options.
         * @param dependencies      A collection of connector dependencies.
         * @return A new instance of the management connector.
         * @throws java.net.MalformedURLException Invalid JMX connection string.
         */
        @Override
        protected JmxConnector newNotificationSupport(final String connectionString,
                                                      final Map<String, String> connectionOptions,
                                                      final RequiredService<?>... dependencies) throws MalformedURLException {
            return new JmxConnector(connectionString, connectionOptions);
        }
    }

    private static final class ProvidedJmxConnectors extends ProvidedManagementConnectors<JmxConnector> {

        /**
         * Creates a new instance of the management connector factory.
         *
         * @param targetName           The The name of the management target.
         * @param activationProperties A collection of activation properties to read.
         * @return A new instance of the management connector factory.
         */
        @Override
        protected ManagementConnectorProvider<JmxConnector> createConnectorFactory(final String targetName,
                                                                                   final ActivationPropertyReader activationProperties) {
            return new JmxConnectorProvider(targetName);
        }
    }

    /**
     * Initializes a new instance of the JMX connector bundle activator.
     */
    @SuppressWarnings("UnusedDeclaration")
    public JmxConnectorBundleActivator() {
        super(JmxConnector.NAME,
                new ProvidedJmxConnectors(),
                JmxConnectorHelpers.getLogger());
    }
}
