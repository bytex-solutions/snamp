package com.snamp.configuration;

import com.snamp.SnampClassTestSet;
import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class JmxConnectorConfigurationDescriptorTest extends SnampClassTestSet<JmxConnectorConfigurationDescriptor> {

    @Test
    public final void attributeConfigDescriptorTest(){
        final ConfigurationEntityDescription<AttributeConfiguration> description = new JmxConnectorConfigurationDescriptor().getDescription(AttributeConfiguration.class);
        final ConfigurationEntityDescription.ParameterDescription param = description.getParameterDescriptor(JmxAttributeConfigurationDescriptor.OBJECT_NAME_PROPERTY);
        final String defValue = param.getDescription(null);//default locale
        assertTrue(defValue.length() > 0);
        final String ruValue = param.getDescription(Locale.forLanguageTag("RU"));
        assertTrue(ruValue.length() > 0);
        assertTrue(!defValue.equals(ruValue));
    }
}
