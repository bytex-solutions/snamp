package com.itworks.snamp.connectors.jmx;

import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.connectors.SelectableConnectorParameterDescriptor;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;

/**
 * Represents JMX connector configuration descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {
    static final String JMX_LOGIN = "login";
    static final String JMX_PASSWORD = "password";
    /**
     * Connection watchdog control period (in milliseconds).
     */
    static final String CONNECTION_CHECK_PERIOD = "connectionCheckPeriod";
    static final String OBJECT_NAME_PROPERTY = "objectName";
    static final String SEVERITY_PARAM = NotificationDescriptor.SEVERITY_PARAM;
    static final String USE_REGEXP_PARAM = "useRegexp";

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "JmxEventConfig";

        private EventConfigurationInfo(){
            super(EventConfiguration.class,
                    OBJECT_NAME_PROPERTY,
                    SEVERITY_PARAM);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc != null ? loc : Locale.getDefault());
        }
    }

    private static final class ConnectorConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration> {
        private static final String RESOURCE_NAME = "JmxConnectorConfig";

        private ConnectorConfigurationInfo() {
            super(ManagedResourceConfiguration.class,
                    JMX_LOGIN,
                    JMX_PASSWORD,
                    CONNECTION_CHECK_PERIOD);
        }

        @Override
        protected ResourceBundle getBundle(final Locale loc) {
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc != null ? loc : Locale.getDefault());
        }
    }

    /**
     * @author Roman Sakno
     * @version 1.0
     * @since 1.0
     */
    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration> {
        private static final String RESOURCE_NAME = "JmxAttributeConfig";

        private final class ObjectNameParameter extends ParameterDescriptionImpl implements SelectableConnectorParameterDescriptor {
            private ObjectNameParameter() {
                super(OBJECT_NAME_PROPERTY);
            }

            @Override
            public String[] suggestValues(final String connectionString,
                                          final Map<String, String> connectionOptions,
                                          final Locale loc) throws IOException {
                final JmxConnectionOptions options = new JmxConnectionOptions(connectionString, connectionOptions);
                try (final JMXConnector connection = options.createConnection()) {
                    final MBeanServerConnection server = connection.getMBeanServerConnection();
                    return ArrayUtils.toArray(Collections2.transform(server.queryNames(null, null),
                            Functions.toStringFunction()), String.class);

                }
            }
        }

        private AttributeConfigurationInfo() {
            super(AttributeConfiguration.class,
                    OBJECT_NAME_PROPERTY,
                    USE_REGEXP_PARAM);
        }

        /**
         * Creates a new resource-based parameter descriptor.
         *
         * @param parameterName The name of the configuration parameter.
         * @return A new instance of descriptor.
         */
        @Override
        protected ParameterDescriptionImpl createParameterDescriptor(final String parameterName) {
            switch (parameterName) {
                case OBJECT_NAME_PROPERTY:
                    return new ObjectNameParameter();
                default:
                    return super.createParameterDescriptor(parameterName);
            }
        }

        /**
         * Retrieves resource accessor for the specified locale.
         *
         * @param loc The requested localization of the resource. May be {@literal null}.
         * @return The resource accessor.
         */
        @Override
        protected final ResourceBundle getBundle(Locale loc) {
            if(loc == null) loc = Locale.getDefault();
            return ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc);
        }
    }

    JmxConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationInfo(), new ConnectorConfigurationInfo(), new EventConfigurationInfo());
    }
}
