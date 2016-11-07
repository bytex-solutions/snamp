package com.bytex.snamp.connector.http;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.md.MessageDrivenConnectorConfigurationDescriptor;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class HttpConnectorConfigurationDescriptor extends MessageDrivenConnectorConfigurationDescriptor {
    protected HttpConnectorConfigurationDescriptor(final ConfigurationEntityDescription<AttributeConfiguration> attributeDescriptor, final ConfigurationEntityDescription<EventConfiguration> eventDescription) {
        super(attributeDescriptor, eventDescription);
    }
}
