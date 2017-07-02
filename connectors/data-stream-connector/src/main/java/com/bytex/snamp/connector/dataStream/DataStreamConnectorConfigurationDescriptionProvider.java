package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.Convert;
import com.bytex.snamp.ResourceReader;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.dataStream.groovy.GroovyNotificationFilterFactory;
import com.bytex.snamp.parser.ParseException;
import com.google.common.collect.ObjectArrays;

import javax.management.Descriptor;
import javax.management.NotificationFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsLong;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;

/**
 * Represents configuration descriptor for message-driven connectors.
 * @since 2.0
 * @version 2.0
 */
public abstract class DataStreamConnectorConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements ManagedResourceDescriptionProvider {
    private static final String SYNC_PERIOD_PARAM = "synchronizationPeriod";
    private static final String RANGE_START_PARAM = "from";
    private static final String RANGE_END_PARAM = "to";
    private static final String CHANNELS_PARAM = "channels";
    private static final String FILTER_PARAM = "filter";
    private static final String GAUGE_TYPE_PARAM = "gauge";
    private static final String HEARTBEAT_PARAM = "heartbeat";

    protected static class ConnectorConfigurationDescription extends ResourceBasedConfigurationEntityDescription<ManagedResourceConfiguration>{
        private static final String RESOURCE_NAME = "ConnectorConfiguration";
        private final ResourceReader fallbackReader;
        private static final String[] DEFAULT_PARAMS = {SYNC_PERIOD_PARAM, HEARTBEAT_PARAM};

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
            return fallbackReader == null ? Optional.empty() : fallbackReader.getString(key, loc);
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

    private final GroovyNotificationFilterFactory filterFactory;

    protected DataStreamConnectorConfigurationDescriptionProvider(final ConfigurationEntityDescription<?>... descriptions){
        super(descriptions);
        filterFactory = createNotificationFilterFactory(getClass().getClassLoader());
    }

    DataStreamConnectorConfigurationDescriptionProvider(){
        super(ConnectorConfigurationDescription.createDefault(), AttributeConfigurationDescription.createDefault(), EventConfigurationDescription.createDefault());
        filterFactory = createNotificationFilterFactory(getClass().getClassLoader());
    }

    private static GroovyNotificationFilterFactory createNotificationFilterFactory(final ClassLoader loader){
        try {
            return new GroovyNotificationFilterFactory(loader);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
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

    static long parseRangeStartAsLong(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, Convert::toLong, DSConnectorAbsentConfigurationParameterException::new)
                .orElseThrow(NumberFormatException::new);
    }

    static long parseRangeEndAsLong(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, Convert::toLong, DSConnectorAbsentConfigurationParameterException::new)
                .orElseThrow(NumberFormatException::new);
    }

    static double parseRangeStartAsDouble(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, DataStreamConnectorConfigurationDescriptionProvider::objToDouble, DSConnectorAbsentConfigurationParameterException::new);
    }

    static double parseRangeEndAsDouble(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, DataStreamConnectorConfigurationDescriptionProvider::objToDouble, DSConnectorAbsentConfigurationParameterException::new);
    }

    static Duration parseRangeStartAsDuration(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_START_PARAM, DataStreamConnectorConfigurationDescriptionProvider::objToDuration, DSConnectorAbsentConfigurationParameterException::new);
    }

    static Duration parseRangeEndAsDuration(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, RANGE_END_PARAM, DataStreamConnectorConfigurationDescriptionProvider::objToDuration, DSConnectorAbsentConfigurationParameterException::new);
    }

    static int parseChannels(final AttributeDescriptor descriptor) {
        return getField(descriptor, CHANNELS_PARAM, Convert::toInt)
                .orElseGet(OptionalInt::empty)
                .orElse(1);
    }

    public SyntheticAttributeFactory parseGaugeType(final AttributeDescriptor descriptor) throws DSConnectorAbsentConfigurationParameterException, ParseException {
        final String gaugeType =
                getFieldIfPresent(descriptor, GAUGE_TYPE_PARAM, String::valueOf, DSConnectorAbsentConfigurationParameterException::new);
        return AttributeParser.parse(gaugeType);
    }

    final NotificationFilter parseNotificationFilter(final Descriptor descriptor) {
        final String filter = getField(descriptor, FILTER_PARAM, String::valueOf).orElse("");
        if (filter.isEmpty())
            return notification -> true;
        return filterFactory.create(filter);
    }

    protected Optional<Duration> getHeartbeat(final Map<String, String> parameters) {
        return getValue(parameters, HEARTBEAT_PARAM, Long::parseLong).map(Duration::ofMillis);
    }
}
