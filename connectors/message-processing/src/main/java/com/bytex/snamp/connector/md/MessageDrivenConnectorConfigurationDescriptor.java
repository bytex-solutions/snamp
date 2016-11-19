package com.bytex.snamp.connector.md;

import com.bytex.snamp.Convert;
import com.bytex.snamp.ResourceReader;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.md.notifications.MeasurementNotification;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.NotificationFilter;
import javax.management.openmbean.CompositeData;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
    public static final String COMPONENT_INSTANCE_PARAM = "componentInstance";
    public static final String COMPONENT_NAME_PARAM = "componentName";
    private static final String SYNC_PERIOD_PARAM = "synchronizationPeriod";
    private static final String RANGE_START_PARAM = "from";
    private static final String RANGE_END_PARAM = "to";
    private static final String CHANNELS_PARAM = "channels";
    private static final String FILTER_PARAM = "filter";

    protected static class ConnectorConfigurationDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorConfiguration";
        private final ResourceReader fallbackReader;
        private static final String[] DEFAULT_ATTRIBUTES = {COMPONENT_INSTANCE_PARAM, COMPONENT_NAME_PARAM, SYNC_PERIOD_PARAM};

        /**
         * Initializes a new resource-based descriptor.
         *
         * @param baseName   The name of the resource.
         * @param parameters An array of configuration parameters.
         */
        protected ConnectorConfigurationDescription(final String baseName, final String... parameters) {
            super(baseName, ManagedResourceConfiguration.class, ObjectArrays.concat(parameters, DEFAULT_ATTRIBUTES, String.class));
            fallbackReader = new ResourceReader(ConnectorConfigurationDescription.class, RESOURCE_NAME);
        }

        private ConnectorConfigurationDescription(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class);
            fallbackReader = null;
        }

        public static ConnectorConfigurationDescription createDefault(){
            return new ConnectorConfigurationDescription();
        }

        @Override
        protected Optional<String> getStringFallback(final String key, final Locale loc) {
            return fallbackReader != null ? fallbackReader.getString(key, loc) : Optional.empty();
        }
    }

    protected static class AttributeConfigurationDescription extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration>{
        private static final String RESOURCE_NAME = "AttributeConfiguration";
        private final ResourceReader fallbackReader;
        private static final String[] DEFAULT_ATTRIBUTES = {RANGE_START_PARAM, RANGE_END_PARAM, CHANNELS_PARAM, FILTER_PARAM};

        /**
         * Initializes a new resource-based descriptor.
         *
         * @param baseName   The name of the resource.
         * @param parameters An array of configuration parameters.
         */
        protected AttributeConfigurationDescription(final String baseName, final String... parameters) {
            super(baseName, AttributeConfiguration.class, ObjectArrays.concat(parameters, DEFAULT_ATTRIBUTES, String.class));
            fallbackReader = new ResourceReader(AttributeConfigurationDescription.class, RESOURCE_NAME);
        }

        private AttributeConfigurationDescription(){
            super(RESOURCE_NAME, AttributeConfiguration.class);
            fallbackReader = null;
        }

        public static AttributeConfigurationDescription createDefault(){
            return new AttributeConfigurationDescription();
        }

        @Override
        protected final Optional<String> getStringFallback(final String key, final Locale loc) {
            return fallbackReader != null ? fallbackReader.getString(key, loc) : null;
        }
    }

    protected MessageDrivenConnectorConfigurationDescriptor(final ConfigurationEntityDescription<?>... descriptions){
        super(descriptions);
    }

    protected MessageDrivenConnectorConfigurationDescriptor(){
        super(ConnectorConfigurationDescription.createDefault(), AttributeConfigurationDescription.createDefault());
    }

    protected String parseComponentInstance(final Map<String, String> parameters){
        return getValue(parameters, COMPONENT_INSTANCE_PARAM, Function.identity(), () -> "");
    }

    protected String parseComponentName(final Map<String, String> parameters) {
        return getValue(parameters, COMPONENT_NAME_PARAM, Function.identity(), () -> firstNonNull(parameters.get(GROUP_NAME_PROPERTY), "DEFAULT"));
    }

    protected Duration parseSyncPeriod(final Map<String, String> parameters) {
        final long period = getValueAsLong(parameters, SYNC_PERIOD_PARAM, Long::parseLong, () -> 5000L);
        return Duration.ofMillis(period);
    }

    private static Double objToDouble(final Object value){
        return Double.parseDouble(String.valueOf(value));
    }

    private static Duration objToDuration(final Object value){
        return Duration.parse(String.valueOf(value));
    }

    static long parseRangeStartAsLong(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, Convert::toLong, MDConnectorAbsentConfigurationParameterException::new);
    }

    static long parseRangeEndAsLong(final AttributeDescriptor descriptor) throws MDConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, Convert::toLong, MDConnectorAbsentConfigurationParameterException::new);
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
        return getField(descriptor, CHANNELS_PARAM, Convert::toLong, () -> 1L);
    }

    static NotificationFilter parseNotificationFilter(final AttributeDescriptor descriptor) throws InvalidSyntaxException {
        final String filter = getField(descriptor, FILTER_PARAM, String::valueOf, () -> "");
        if(filter.isEmpty())
            return notification -> true;
        final Filter ldapFilter = FrameworkUtil.createFilter(filter);
        return notification -> {
            final Map<String, ?> dataToFilter;
            if(notification instanceof MeasurementNotification<?>)
                dataToFilter = ((MeasurementNotification<?>) notification).getMeasurement().getUserData();
            else if(notification.getUserData() instanceof CompositeData)
                dataToFilter = CompositeDataUtils.toMap((CompositeData) notification.getUserData());
            else
                dataToFilter = ImmutableMap.of("userData", notification.getUserData());
            return ldapFilter.matches(dataToFilter);
        };
    }
}
