package com.bytex.snamp.adapters.http;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;

import com.bytex.snamp.configuration.ResourceAdapterConfiguration;
import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents descriptor of REST adapter configuration scheme.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class HttpAdapterConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {

    private static final String DATE_FORMAT_PARAM = "dateFormat";

    private static final class AdapterConfigurationInfo extends ResourceBasedConfigurationEntityDescription<ResourceAdapterConfiguration> {
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
