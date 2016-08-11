package com.bytex.snamp.gateway.http;

import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;

import javax.management.Descriptor;

import static com.bytex.snamp.jmx.DescriptorUtils.getField;
import static com.bytex.snamp.jmx.DescriptorUtils.hasField;

/**
 * Represents descriptor of REST adapter configuration scheme.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class HttpGatewayConfigurationDescriptor extends ConfigurationEntityDescriptionProviderImpl {

    private static final String DATE_FORMAT_PARAM = "dateFormat";

    private static final class GatewayConfigurationInfo extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration> {
        private static final String RESOURCE_NAME = "HttpGatewayConfig";

        private GatewayConfigurationInfo(){
            super(RESOURCE_NAME,
                    GatewayConfiguration.class,
                    DATE_FORMAT_PARAM);
        }
    }

    HttpGatewayConfigurationDescriptor(){
        super(new GatewayConfigurationInfo());
    }

    static String parseDateFormatParam(final Descriptor descr){
        if(hasField(descr, DATE_FORMAT_PARAM))
            return getField(descr, DATE_FORMAT_PARAM, String.class);
        else return null;
    }
}
