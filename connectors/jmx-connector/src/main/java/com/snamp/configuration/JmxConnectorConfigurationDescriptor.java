package com.snamp.configuration;

/**
 * Represents JMX connector configuration descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxConnectorConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl  {
    public JmxConnectorConfigurationDescriptor(){
        super(new JmxAttributeConfigurationDescriptor());
    }
}
