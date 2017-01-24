package com.bytex.snamp.connector.dsp;

import com.bytex.snamp.Convert;
import com.bytex.snamp.ResourceReader;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.dsp.notifications.MeasurementNotification;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.parser.ParseException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ObjectArrays;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;

import javax.management.Descriptor;
import javax.management.NotificationFilter;
import javax.management.openmbean.CompositeData;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;

/**
 * Represents configuration descriptor for message-driven connectors.
 * @since 2.0
 * @version 2.0
 */
public abstract class DataStreamDrivenConnectorConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String SYNC_PERIOD_PARAM = "synchronizationPeriod";
    private static final String RANGE_START_PARAM = "from";
    private static final String RANGE_END_PARAM = "to";
    private static final String CHANNELS_PARAM = "channels";
    private static final String FILTER_PARAM = "filter";
    private static final String GAUGE_TYPE_PARAM = "gauge";

    protected static class ConnectorConfigurationDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorConfiguration";
        private final ResourceReader fallbackReader;
        private static final String[] DEFAULT_PARAMS = {SYNC_PERIOD_PARAM};

        /**
         * Initializes a new resource-based descriptor.
         *
         * @param baseName   The name of the resource.
         * @param parameters An array of configuration parameters.
         */
        protected ConnectorConfigurationDescription(final String baseName, final String... parameters) {
            super(baseName, ManagedResourceConfiguration.class, ObjectArrays.concat(parameters, DEFAULT_PARAMS, String.class));
            fallbackReader = new ResourceReader(ConnectorConfigurationDescription.class, RESOURCE_NAME);
        }

        private ConnectorConfigurationDescription(){
            super(RESOURCE_NAME, ManagedResourceConfiguration.class, DEFAULT_PARAMS);
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
        private static final String[] DEFAULT_PARAMS = {RANGE_START_PARAM, RANGE_END_PARAM, CHANNELS_PARAM, FILTER_PARAM, GAUGE_TYPE_PARAM};

        /**
         * Initializes a new resource-based descriptor.
         *
         * @param baseName   The name of the resource.
         * @param parameters An array of configuration parameters.
         */
        protected AttributeConfigurationDescription(final String baseName, final String... parameters) {
            super(baseName, AttributeConfiguration.class, ObjectArrays.concat(parameters, DEFAULT_PARAMS, String.class));
            fallbackReader = new ResourceReader(AttributeConfigurationDescription.class, RESOURCE_NAME);
        }

        private AttributeConfigurationDescription(){
            super(RESOURCE_NAME, AttributeConfiguration.class, DEFAULT_PARAMS);
            fallbackReader = null;
        }

        public static AttributeConfigurationDescription createDefault(){
            return new AttributeConfigurationDescription();
        }

        @Override
        protected final Optional<String> getStringFallback(final String key, final Locale loc) {
            return fallbackReader != null ? fallbackReader.getString(key, loc) : Optional.empty();
        }
    }

    protected static class EventConfigurationDescription extends ResourceBasedConfigurationEntityDescription<EventConfiguration>{
        private static final String RESOURCE_NAME = "EventConfiguration";
        private final ResourceReader fallbackReader;
        private static final String[] DEFAULT_PARAMS = {FILTER_PARAM};

        /**
         * Initializes a new resource-based descriptor.
         *
         * @param baseName   The name of the resource.
         * @param parameters An array of configuration parameters.
         */
        protected EventConfigurationDescription(final String baseName, final String... parameters) {
            super(baseName, EventConfiguration.class, ObjectArrays.concat(parameters, DEFAULT_PARAMS, String.class));
            fallbackReader = new ResourceReader(AttributeConfigurationDescription.class, RESOURCE_NAME);
        }

        private EventConfigurationDescription(){
            super(RESOURCE_NAME, EventConfiguration.class, DEFAULT_PARAMS);
            fallbackReader = null;
        }

        public static EventConfigurationDescription createDefault(){
            return new EventConfigurationDescription();
        }

        @Override
        protected final Optional<String> getStringFallback(final String key, final Locale loc) {
            return fallbackReader != null ? fallbackReader.getString(key, loc) : Optional.empty();
        }
    }

    protected DataStreamDrivenConnectorConfigurationDescriptionProvider(final ConfigurationEntityDescription<?>... descriptions){
        super(descriptions);
    }

    protected DataStreamDrivenConnectorConfigurationDescriptionProvider(){
        super(ConnectorConfigurationDescription.createDefault(), AttributeConfigurationDescription.createDefault(), EventConfigurationDescription.createDefault());
    }

    protected Duration parseSyncPeriod(final Map<String, String> parameters) {
        final long period = getValueAsLong(parameters, SYNC_PERIOD_PARAM, Long::parseLong).orElse(5000L);
        return Duration.ofMillis(period);
    }

    private static Double objToDouble(final Object value){
        return Double.parseDouble(String.valueOf(value));
    }

    private static Duration objToDuration(final Object value){
        return Duration.parse(String.valueOf(value));
    }

    static long parseRangeStartAsLong(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, Convert::toLong, DSPConnectorAbsentConfigurationParameterException::new);
    }

    static long parseRangeEndAsLong(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, Convert::toLong, DSPConnectorAbsentConfigurationParameterException::new);
    }

    static double parseRangeStartAsDouble(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, DataStreamDrivenConnectorConfigurationDescriptionProvider::objToDouble, DSPConnectorAbsentConfigurationParameterException::new);
    }

    static double parseRangeEndAsDouble(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, DataStreamDrivenConnectorConfigurationDescriptionProvider::objToDouble, DSPConnectorAbsentConfigurationParameterException::new);
    }

    static Duration parseRangeStartAsDuration(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, DataStreamDrivenConnectorConfigurationDescriptionProvider::objToDuration, DSPConnectorAbsentConfigurationParameterException::new);
    }

    static Duration parseRangeEndAsDuration(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, DataStreamDrivenConnectorConfigurationDescriptionProvider::objToDuration, DSPConnectorAbsentConfigurationParameterException::new);
    }

    static long parseChannels(final AttributeDescriptor descriptor){
        return getField(descriptor, CHANNELS_PARAM, Convert::toLong).orElse(1L);
    }

    public DataStreamDrivenAttributeFactory parseGaugeType(final AttributeDescriptor descriptor) throws DSPConnectorAbsentConfigurationParameterException, ParseException {
        final String gaugeType =
                getFieldIfPresent(descriptor, GAUGE_TYPE_PARAM, String::valueOf, DSPConnectorAbsentConfigurationParameterException::new);
        return AttributeParser.parse(gaugeType);
    }

    static NotificationFilter parseNotificationFilter(final Descriptor descriptor) throws InvalidSyntaxException {
        final String filter = getField(descriptor, FILTER_PARAM, String::valueOf).orElse("");
        if (filter.isEmpty()) {
            return notification -> true;
        } else {
            final Filter ldapFilter = FrameworkUtil.createFilter(filter);
            return notification -> {
                final Map<String, ?> dataToFilter;
                if (notification instanceof MeasurementNotification<?>)
                    dataToFilter = ((MeasurementNotification<?>) notification).getMeasurement().getAnnotations();
                else if (notification.getUserData() instanceof CompositeData)
                    dataToFilter = CompositeDataUtils.toMap((CompositeData) notification.getUserData());
                else
                    dataToFilter = ImmutableMap.of("userData", notification.getUserData());
                return ldapFilter.matches(dataToFilter);
            };
        }
    }
}
