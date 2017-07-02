package com.bytex.snamp.connector.dataStream;

import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

import static com.bytex.snamp.connector.dataStream.DataStreamConnectorConfigurationDescriptionProvider.AttributeConfigurationDescription;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ConfigurationDescriptorTest extends Assert {
    @Test
    public void attributeDescriptorTest(){
        final AttributeConfigurationDescription description = new AttributeConfigurationDescription("TestResource");
        final ConfigurationEntityDescription.ParameterDescription filterParam = description.getParameterDescriptor("filter");
        assertNotNull(filterParam);
        assertNotNull(filterParam.toString(Locale.getDefault()));
    }
}
