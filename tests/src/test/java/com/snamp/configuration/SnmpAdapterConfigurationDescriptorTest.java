package com.snamp.configuration;

import com.snamp.SnampClassTestSet;
import com.snamp.adapters.Adapter;
import org.junit.Test;

import java.util.Locale;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnmpAdapterConfigurationDescriptorTest extends SnampClassTestSet<SnmpAdapterConfigurationDescriptor> {
    @Test
    public final void hostingConfugurationDescriptors(){
        final ConfigurationEntityDescription<AgentConfiguration.HostingConfiguration> description = new SnmpAdapterConfigurationDescriptor().getDescription(AgentConfiguration.HostingConfiguration.class);
        final ConfigurationEntityDescription.ParameterDescription param = description.getParameterDescriptor(SnmpAdapterConfigurationDescriptor.SNMPv3_GROUPS_PARAM);
        final String defValue = param.getDescription(null);//default locale
        assertTrue(defValue.length() > 100);
        final String ruValue = param.getDescription(Locale.forLanguageTag("RU"));
        assertTrue(ruValue.length() > 100);
        assertTrue(!defValue.equals(ruValue));
        assertNotNull(description.getParameterDescriptor(SnmpAdapterConfigurationDescriptor.SOCKET_TIMEOUT_PARAM));
        assertNotNull(description.getParameterDescriptor(Adapter.ADDRESS_PARAM_NAME));
    }

    @Test
    public final void attributesConfigurationDescriptors(){
        final ConfigurationEntityDescription<AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration> description = new SnmpAdapterConfigurationDescriptor().getDescription(AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration.class);
        final ConfigurationEntityDescription.ParameterDescription param = description.getParameterDescriptor(SnmpAdapterConfigurationDescriptor.DATE_TIME_DISPLAY_FORMAT_PARAM);
        final String defValue = param.getDescription(null);//default locale
        assertTrue(defValue.length() > 10);
        final String ruValue = param.getDescription(Locale.forLanguageTag("RU"));
        assertTrue(ruValue.length() > 10);
        assertTrue(!defValue.equals(ruValue));
    }

    @Test
    public final void eventConfigurationDescriptors(){
        final ConfigurationEntityDescription<AgentConfiguration.ManagementTargetConfiguration.EventConfiguration> description = new SnmpAdapterConfigurationDescriptor().getDescription(AgentConfiguration.ManagementTargetConfiguration.EventConfiguration.class);
        final ConfigurationEntityDescription.ParameterDescription param = description.getParameterDescriptor(SnmpAdapterConfigurationDescriptor.TARGET_ADDRESS_PARAM);
        final String defValue = param.getDescription(null);//default locale
        assertTrue(defValue.length() > 10);
        final String ruValue = param.getDescription(Locale.forLanguageTag("RU"));
        assertTrue(ruValue.length() > 10);
        assertTrue(!defValue.equals(ruValue));
    }
}
