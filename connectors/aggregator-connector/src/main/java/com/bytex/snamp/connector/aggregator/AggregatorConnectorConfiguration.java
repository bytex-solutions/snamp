package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import java.time.Duration;
import java.util.Map;
import static com.bytex.snamp.MapUtils.*;

/**
 * Provides configuration schema of this resource connector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class AggregatorConnectorConfiguration extends ConfigurationEntityDescriptionProviderImpl {
    static final String SOURCE_PARAM = "source";
    static final String FOREIGN_ATTRIBUTE_PARAM = "foreignAttribute";
    static final String FIRST_FOREIGN_ATTRIBUTE_PARAM = "firstForeignAttribute";
    static final String SECOND_FOREIGN_ATTRIBUTE_PARAM = "secondForeignAttribute";
    static final String COMPARER_PARAM = "comparer";
    static final String VALUE_PARAM = "value";
    private static final String TIME_INTERVAL_PARAM = "timeInterval";
    static final String FIELD_PATH_PARAM = "fieldPath";
    private static final String NOTIFICATION_FREQUENCY_PARAM = "notificationFrequency";

    private static final class ResourceConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorParameters";

        private ResourceConfigurationDescriptor(){
            super(RESOURCE_NAME,
                    ManagedResourceConfiguration.class,
                    NOTIFICATION_FREQUENCY_PARAM);
        }
    }

    private static final class EventConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventParameters";

        private EventConfigurationDescriptor(){
            super(RESOURCE_NAME,
                    EventConfiguration.class,
                    FOREIGN_ATTRIBUTE_PARAM,
                    SOURCE_PARAM);
        }
    }

    private static final class AttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeParameters";

        private AttributeConfigurationDescriptor(){
            super(RESOURCE_NAME,
                    AttributeConfiguration.class,
                    SOURCE_PARAM,
                    FOREIGN_ATTRIBUTE_PARAM,
                    FIRST_FOREIGN_ATTRIBUTE_PARAM,
                    SECOND_FOREIGN_ATTRIBUTE_PARAM,
                    COMPARER_PARAM,
                    VALUE_PARAM,
                    TIME_INTERVAL_PARAM);
        }
    }

    AggregatorConnectorConfiguration(){
        super(new AttributeConfigurationDescriptor(),
                new EventConfigurationDescriptor(),
                new ResourceConfigurationDescriptor());
    }

    private static String getAttributeParameter(final AttributeDescriptor descriptor,
                                                final String parameterName) throws AbsentAggregatorAttributeParameterException {
        if(descriptor.hasField(parameterName))
            return descriptor.getField(parameterName, String.class);
        else throw new AbsentAggregatorAttributeParameterException(parameterName);
    }

    private static String getNotificationParameter(final NotificationDescriptor descriptor,
                                                final String parameterName) throws AbsentAggregatorNotificationParameterException {
        if(descriptor.hasField(parameterName))
            return descriptor.getField(parameterName, String.class);
        else throw new AbsentAggregatorNotificationParameterException(parameterName);
    }

    static String getSourceManagedResource(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getAttributeParameter(descriptor, SOURCE_PARAM);
    }

    static String getSourceManagedResource(final NotificationDescriptor descriptor) throws AbsentAggregatorNotificationParameterException {
        return getNotificationParameter(descriptor, SOURCE_PARAM);
    }

    static String getForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getAttributeParameter(descriptor, FOREIGN_ATTRIBUTE_PARAM);
    }

    static String getForeignAttributeName(final NotificationDescriptor descriptor) throws AbsentAggregatorNotificationParameterException {
        return getNotificationParameter(descriptor, FOREIGN_ATTRIBUTE_PARAM);
    }

    static String getFirstForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getAttributeParameter(descriptor, FIRST_FOREIGN_ATTRIBUTE_PARAM);
    }

    static String getSecondForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getAttributeParameter(descriptor, SECOND_FOREIGN_ATTRIBUTE_PARAM);
    }

    static Comparison getComparisonType(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return Comparison.parse(getAttributeParameter(descriptor, COMPARER_PARAM));
    }

    static String getUserDefinedValue(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getAttributeParameter(descriptor, VALUE_PARAM);
    }

    static long getTimeIntervalInMillis(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return Long.parseLong(getAttributeParameter(descriptor, TIME_INTERVAL_PARAM));
    }

    static CompositeDataPath getFieldPath(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        if(descriptor.hasField(FIELD_PATH_PARAM))
            return new CompositeDataPath(descriptor.getField(FIELD_PATH_PARAM, String.class));
        else throw new AbsentAggregatorAttributeParameterException(FIELD_PATH_PARAM);
    }

    static Duration getNotificationFrequency(final Map<String, String> parameters){
        final long frequency = getValueAsLong(parameters, NOTIFICATION_FREQUENCY_PARAM, Long::parseLong, () -> 5000);
        return Duration.ofMillis(frequency);
    }
}
