package com.itworks.snamp.adapters.http;

import com.itworks.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.itworks.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.itworks.snamp.configuration.ThreadPoolConfigurationDescriptor;

import javax.management.Descriptor;

import static com.itworks.snamp.configuration.AgentConfiguration.ResourceAdapterConfiguration;
import static com.itworks.snamp.jmx.DescriptorUtils.getField;
import static com.itworks.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents descriptor of REST adapter configuration scheme.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {

    private static final String DATE_FORMAT_PARAM = "dateFormat";

    private static final class AdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> implements ThreadPoolConfigurationDescriptor<ResourceAdapterConfiguration> {
        private static final String RESOURCE_NAME = "RestAdapterConfig";

        private AdapterConfigurationInfo(){
            super(RESOURCE_NAME,
                    ResourceAdapterConfiguration.class,
                    DATE_FORMAT_PARAM);
        }
    }

    HttpAdapterConfigurationDescriptor(){
        super(new AdapterConfigurationInfo());
    }

    static String parseDateFormatParam(final Descriptor descr){
        if(hasField(descr, DATE_FORMAT_PARAM))
            return getField(descr, DATE_FORMAT_PARAM, String.class);
        else return null;
    }
}
