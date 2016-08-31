package com.bytex.snamp.connector.md;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.ManagedResourceDescriptionProvider;

import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.GROUP_NAME_PROPERTY;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.bytex.snamp.MapUtils.getValue;

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

    protected MessageDrivenConnectorConfigurationDescriptor(final ConfigurationEntityDescription<AttributeConfiguration> attributeDescriptor,
                                                            final ConfigurationEntityDescription<EventConfiguration> eventDescription){
        super(attributeDescriptor, eventDescription);
    }

    final String parseComponentInstance(final Map<String, String> parameters, final String resourceName){
        return getValue(parameters, COMPONENT_INSTANCE_PARAM, Function.identity(), () -> resourceName);
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
}
