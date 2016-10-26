package com.bytex.snamp.connector.md;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.GROUP_NAME_PROPERTY;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.bytex.snamp.MapUtils.*;
import static com.bytex.snamp.jmx.DescriptorUtils.getFieldIfPresent;

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
    protected static final String ATTRIBUTE_TYPE_PARAM = "attributeType";

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

    final NotificationParser createNotificationParser(final Map<String, String> parameters){
        if(parameters.containsKey(PARSER_LANGUAGE_PARAM) && parameters.containsKey(PARSER_SCRIPT_PARAM)){
            return null;
        } else
            return new DefaultNotificationParser();
    }

    protected MessageDrivenConnectorAbsentConfigurationParameterException absentConfigurationParameter(final String paramName){
        return new MessageDrivenConnectorAbsentConfigurationParameterException(paramName);
    }

    public final String parseAttributeType(final AttributeDescriptor descriptor) throws MessageDrivenConnectorAbsentConfigurationParameterException {
        return getFieldIfPresent(descriptor, ATTRIBUTE_TYPE_PARAM, Objects::toString, this::absentConfigurationParameter);
    }

    protected Duration parseSyncPeriod(final Map<String, String> parameters) {
        final long period = getValueAsLong(parameters, SYNC_PERIOD_PARAM, Long::parseLong, () -> 5000L);
        return Duration.ofMillis(period);
    }
}
