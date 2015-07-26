package com.bytex.snamp.connectors.jmx;

import com.google.common.base.Functions;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.connectors.SelectableConnectorParameterDescriptor;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.jmx.DescriptorUtils;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration;
import static com.bytex.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration.EventConfiguration;
import static com.bytex.snamp.connectors.ManagedResourceConnector.SMART_MODE_PARAM;
import static com.bytex.snamp.connectors.ConfigurationEntityRuntimeMetadata.AUTOMATICALLY_ADDED_FIELD;

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
    private static final String USE_REGEXP_PARAM = "useRegexp";

    private static final Splitter SIGNATURE_SPLITTER = Splitter.on(',').omitEmptyStrings().trimResults();
    private static final String SIGNATURE_PARAM = "signature";

    private static final class EventConfigurationInfo extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "JmxEventConfig";

        private EventConfigurationInfo(){
            super(RESOURCE_NAME,
                    EventConfiguration.class,
                    OBJECT_NAME_PROPERTY,
                    SEVERITY_PARAM,
                    USE_REGEXP_PARAM);
        }
    }

    private static final class ConnectorConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration> {
        private static final String RESOURCE_NAME = "JmxConnectorConfig";

        private ConnectorConfigurationInfo() {
            super(RESOURCE_NAME,
                    ManagedResourceConfiguration.class,
                    JMX_LOGIN,
                    JMX_PASSWORD,
                    CONNECTION_CHECK_PERIOD,
                    SMART_MODE_PARAM,
                    OBJECT_NAME_PROPERTY);
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
                                          final Locale loc) throws IOException, MalformedObjectNameException {
                final JmxConnectionOptions options = new JmxConnectionOptions(connectionString, connectionOptions);
                try (final JMXConnector connection = options.createConnection()) {
                    final MBeanServerConnection server = connection.getMBeanServerConnection();
                    return ArrayUtils.toArray(Collections2.transform(server.queryNames(null, null),
                            Functions.toStringFunction()), String.class);

                }
            }
        }

        private AttributeConfigurationInfo() {
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
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
    }

    JmxConnectorConfigurationDescriptor(){
        super(new AttributeConfigurationInfo(), new ConnectorConfigurationInfo(), new EventConfigurationInfo());
    }

    static boolean useRegexpOption(final Descriptor options) {
        return DescriptorUtils.hasField(options, USE_REGEXP_PARAM) &&
                Objects.equals(Boolean.TRUE.toString(), DescriptorUtils.getField(options, USE_REGEXP_PARAM, String.class));
    }

    static boolean checkSignature(final Descriptor options,
                                  final MBeanParameterInfo[] parameters){
        if(DescriptorUtils.hasField(options, SIGNATURE_PARAM)){
            final List<String> expectedSignature = SIGNATURE_SPLITTER.splitToList(DescriptorUtils.getField(options, SIGNATURE_PARAM, String.class));
            if(parameters.length == expectedSignature.size()){
                for(int i =0; i < parameters.length; i++)
                    if(!Objects.equals(expectedSignature.get(i), parameters[i].getType()))
                        return false;
                return true;
            }
            else return false;
        }
        else return true;
    }

    static ObjectName getObjectName(final Descriptor descriptor) throws MalformedObjectNameException, JmxAbsentConfigurationParameterException {
        if(DescriptorUtils.hasField(descriptor, OBJECT_NAME_PROPERTY))
            return new ObjectName(DescriptorUtils.getField(descriptor, OBJECT_NAME_PROPERTY, String.class));
        else throw new JmxAbsentConfigurationParameterException(OBJECT_NAME_PROPERTY);
    }

    static ObjectName getObjectName(final Map<String, String> parameters) throws MalformedObjectNameException{
        if(parameters.containsKey(OBJECT_NAME_PROPERTY))
            return new ObjectName(parameters.get(OBJECT_NAME_PROPERTY));
        else return null;
    }

    static CompositeData toConfigurationParameters(final ObjectName name) throws OpenDataException {
        return CompositeDataUtils.create(
                ImmutableMap.of(
                        OBJECT_NAME_PROPERTY, name.getCanonicalName(),
                        AUTOMATICALLY_ADDED_FIELD, Boolean.TRUE.toString()
                ), SimpleType.STRING);
    }
}
