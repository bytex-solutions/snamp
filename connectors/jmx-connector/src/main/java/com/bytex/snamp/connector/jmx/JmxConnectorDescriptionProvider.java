package com.bytex.snamp.connector.jmx;

import com.bytex.snamp.Convert;
import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.SelectableConnectorParameterDescriptor;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.google.common.base.Splitter;

import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static com.bytex.snamp.MapUtils.getValueAsInt;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.SMART_MODE_KEY;
import static com.bytex.snamp.jmx.DescriptorUtils.*;

/**
 * Represents JMX connector configuration descriptor.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class JmxConnectorDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String JMX_LOGIN = "login";
    private static final String JMX_PASSWORD = "password";
    /**
     * Connection watchdog control period (in milliseconds).
     */
    private static final String CONNECTION_CHECK_PERIOD = "connectionCheckPeriod";
    static final String OBJECT_NAME_PROPERTY = "objectName";
    private static final String SEVERITY_PARAM = NotificationDescriptor.SEVERITY_PARAM;
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
                    SMART_MODE_KEY,
                    OBJECT_NAME_PROPERTY);
        }
    }

    /**
     * @author Roman Sakno
     * @version 2.0
     * @since 1.0
     */
    private static final class AttributeConfigurationInfo extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration> {
        private static final String RESOURCE_NAME = "JmxAttributeConfig";

        private final class ObjectNameParameter extends ParameterDescriptionImpl implements SelectableConnectorParameterDescriptor {
            private ObjectNameParameter() {
                super(OBJECT_NAME_PROPERTY);
            }

            @Override
            public Set<String> suggestValues(final String connectionString,
                                                   final Map<String, String> connectionOptions,
                                                   final Locale loc) throws IOException, MalformedObjectNameException {
                final JmxConnectionOptions options = new JmxConnectionOptions(connectionString, connectionOptions);
                final Set<String> result = new HashSet<>();
                try (final JMXConnector connection = options.createConnection()) {
                    final MBeanServerConnection server = connection.getMBeanServerConnection();
                    server.queryNames(null, null).stream().map(ObjectName::toString).forEach(result::add);
                }
                return result;
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

    private static final LazyReference<JmxConnectorDescriptionProvider> INSTANCE = LazyReference.soft();

    private JmxConnectorDescriptionProvider(){
        super(new AttributeConfigurationInfo(), new ConnectorConfigurationInfo(), new EventConfigurationInfo());
    }

    static JmxConnectorDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(JmxConnectorDescriptionProvider::new);
    }

    static boolean useRegexpOption(final Descriptor options) {
        return getField(options, USE_REGEXP_PARAM, Convert::toBoolean)
                .orElseGet(Optional::empty)
                .orElse(false);
    }

    static boolean checkSignature(final Descriptor options,
                                  final MBeanParameterInfo[] parameters){
        if(hasField(options, SIGNATURE_PARAM)){
            final List<String> expectedSignature = SIGNATURE_SPLITTER.splitToList(getField(options, SIGNATURE_PARAM, Objects::toString).orElse(""));
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
        final String objectName = getFieldIfPresent(descriptor, OBJECT_NAME_PROPERTY, Objects::toString, JmxAbsentConfigurationParameterException::new);
        return new ObjectName(objectName);
    }

    int parseWatchDogPeriod(final Map<String, String> parameters){
        return getValueAsInt(parameters, CONNECTION_CHECK_PERIOD, Integer::parseInt).orElse(3000);
    }

    ObjectName parseRootObjectName(final Map<String, String> parameters) throws MalformedObjectNameException{
        return parameters.containsKey(OBJECT_NAME_PROPERTY) ? new ObjectName(parameters.get(OBJECT_NAME_PROPERTY)) : null;
    }

    void parseUserNameAndPassword(final Map<String, String> parameters, final Consumer<? super String> userName, final Consumer<? super String> password){
        if(parameters.containsKey(JMX_LOGIN) && parameters.containsKey(JMX_PASSWORD)){
            userName.accept(parameters.get(JMX_LOGIN));
            password.accept(parameters.get(JMX_PASSWORD));
        }
        else {
            userName.accept("");
            password.accept("");
        }
    }
}
