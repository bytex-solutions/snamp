package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.GatewayConfiguration;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("gateway")
public final class GatewayDataObject extends AbstractTypedDataObject<GatewayConfiguration> implements GatewayConfiguration {
    public GatewayDataObject(){

    }

    public GatewayDataObject(final GatewayConfiguration configuration){
        super(configuration);
    }
}
