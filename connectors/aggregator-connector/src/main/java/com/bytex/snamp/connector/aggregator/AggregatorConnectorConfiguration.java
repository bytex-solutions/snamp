package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.MapUtils.*;
import static com.bytex.snamp.jmx.DescriptorUtils.*;

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

    static String getSourceManagedResource(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, SOURCE_PARAM, Objects::toString, AbsentAggregatorAttributeParameterException::new);
    }

    static String getSourceManagedResource(final NotificationDescriptor descriptor) throws AbsentAggregatorNotificationParameterException {
        return getFieldIfPresent(descriptor, SOURCE_PARAM, Objects::toString, AbsentAggregatorNotificationParameterException::new);
    }

    static String getForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, FOREIGN_ATTRIBUTE_PARAM, Objects::toString, AbsentAggregatorAttributeParameterException::new);
    }

    static String getForeignAttributeName(final NotificationDescriptor descriptor) throws AbsentAggregatorNotificationParameterException {
        return getFieldIfPresent(descriptor, FOREIGN_ATTRIBUTE_PARAM, Objects::toString, AbsentAggregatorNotificationParameterException::new);
    }

    static String getFirstForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, FIRST_FOREIGN_ATTRIBUTE_PARAM, Objects::toString, AbsentAggregatorAttributeParameterException::new);
    }

    static String getSecondForeignAttributeName(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, SECOND_FOREIGN_ATTRIBUTE_PARAM, Objects::toString, AbsentAggregatorAttributeParameterException::new);
    }

    static Comparison getComparisonType(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, COMPARER_PARAM, value -> Comparison.parse(value.toString()), AbsentAggregatorAttributeParameterException::new);
    }

    static String getUserDefinedValue(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, VALUE_PARAM, Objects::toString, AbsentAggregatorAttributeParameterException::new);
    }

    static long getTimeIntervalInMillis(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, TIME_INTERVAL_PARAM, value -> Long.parseLong(value.toString()), AbsentAggregatorAttributeParameterException::new);
    }

    static CompositeDataPath getFieldPath(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        return getFieldIfPresent(descriptor, FIELD_PATH_PARAM, value -> new CompositeDataPath(value.toString()), AbsentAggregatorAttributeParameterException::new);
    }

    static Duration getNotificationFrequency(final Map<String, String> parameters){
        final long frequency = getValueAsLong(parameters, NOTIFICATION_FREQUENCY_PARAM, Long::parseLong, () -> 5000);
        return Duration.ofMillis(frequency);
    }
}
