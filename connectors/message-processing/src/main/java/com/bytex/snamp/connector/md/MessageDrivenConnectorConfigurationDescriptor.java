package com.bytex.snamp.connector.md;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.NotificationFilter;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.GROUP_NAME_PROPERTY;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;
import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Represents configuration descriptor for message-driven connectors.
 * @since 2.0
 * @version 2.0
 */
public abstract class MessageDrivenConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    protected static final String COMPONENT_INSTANCE_PARAM = "componentInstance";
    protected static final String COMPONENT_NAME_PARAM = "componentName";
    protected static final String PARSER_LANGUAGE_PARAM = "parserLanguage";
    protected static final String PARSER_SCRIPT_PARAM = "parserScript";
    protected static final String SYNC_PERIOD_PARAM = "synchronizationPeriod";
    protected static final String RANGE_START_PARAM = "from";
    protected static final String RANGE_END_PARAM = "to";
    protected static final String CHANNELS_PARAM = "channels";
    protected static final String FILTER_PARAM = "filter";

    protected MessageDrivenConnectorConfigurationDescriptor(final ConfigurationEntityDescription<AttributeConfiguration> attributeDescriptor,
                                                            final ConfigurationEntityDescription<EventConfiguration> eventDescription){
        super(attributeDescriptor, eventDescription);
    }

    final String parseComponentInstance(final Map<String, String> parameters, final String defaultValue){
        return getValue(parameters, COMPONENT_INSTANCE_PARAM, Function.identity(), () -> defaultValue);
    }

    final String parseComponentName(final Map<String, String> parameters) {
        return getValue(parameters, COMPONENT_NAME_PARAM, Function.identity(), () -> firstNonNull(parameters.get(GROUP_NAME_PROPERTY), "DEFAULT"));
    }

    protected Duration parseSyncPeriod(final Map<String, String> parameters) {
        final long period = getValueAsLong(parameters, SYNC_PERIOD_PARAM, Long::parseLong, () -> 5000L);
        return Duration.ofMillis(period);
    }

    private static Long objToLong(final Object value){
        return Long.parseLong(String.valueOf(value));
    }

    private static Double objToDouble(final Object value){
        return Double.parseDouble(String.valueOf(value));
    }

    private static Duration objToDuration(final Object value){
        return Duration.parse(String.valueOf(value));
    }

    static long parseRangeStartAsLong(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToLong, MDConnectorAbsentConfigurationParameterException::new);
    }

    static long parseRangeEndAsLong(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToLong, MDConnectorAbsentConfigurationParameterException::new);
    }

    static double parseRangeStartAsDouble(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToDouble, MDConnectorAbsentConfigurationParameterException::new);
    }

    static double parseRangeEndAsDouble(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToDouble, MDConnectorAbsentConfigurationParameterException::new);
    }

    static Duration parseRangeStartAsDuration(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToDuration, MDConnectorAbsentConfigurationParameterException::new);
    }

    static Duration parseRangeEndAsDuration(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToDuration, MDConnectorAbsentConfigurationParameterException::new);
    }

    static long parseChannels(final AttributeDescriptor descriptor){
        return getField(descriptor, CHANNELS_PARAM, MessageDrivenConnectorConfigurationDescriptor::objToLong, () -> 1L);
    }

    static NotificationFilter parseNotificationFilter(final AttributeDescriptor descriptor){
        final String filter = getField(descriptor, FILTER_PARAM, String::valueOf, () -> "");
        if(filter.isEmpty())
            return notification -> true;
        final Predicate<String> messageFilter = Pattern.compile(filter).asPredicate();
        return notification -> messageFilter.test(notification.getMessage());
    }
}
