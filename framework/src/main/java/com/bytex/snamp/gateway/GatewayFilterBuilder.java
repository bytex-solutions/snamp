package com.bytex.snamp.gateway;

import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.core.SimpleFilterBuilder;
import org.osgi.framework.ServiceReference;

import static com.bytex.snamp.gateway.Gateway.CATEGORY_PROPERTY;
import static com.bytex.snamp.gateway.Gateway.TYPE_CAPABILITY_ATTRIBUTE;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder of OSGi filter used to query instances of {@link Gateway}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GatewayFilterBuilder extends SimpleFilterBuilder {
    private static final String CATEGORY = "gateway";
    private static final String NAME_PROPERTY = "instanceName";
    private static final long serialVersionUID = -7919619948002475215L;

    GatewayFilterBuilder(){
        put(CATEGORY_PROPERTY, CATEGORY);
    }

    GatewayFilterBuilder(final GatewayConfiguration configuration) {
        super(configuration);
        setGatewayType(configuration.getType()).put(CATEGORY_PROPERTY, CATEGORY);
    }

    public GatewayFilterBuilder setInstanceName(final String value) {
        if (isNullOrEmpty(value))
            remove(NAME_PROPERTY);
        else
            put(NAME_PROPERTY, value);
        return this;
    }

    public GatewayFilterBuilder setGatewayType(final String value){
        if(isNullOrEmpty(value))
            remove(TYPE_CAPABILITY_ATTRIBUTE);
        else
            put(TYPE_CAPABILITY_ATTRIBUTE, value);
        return this;
    }

    static String getGatewayInstance(final ServiceReference<Gateway> gatewayInstance) {
        return getReferencePropertyAsString(gatewayInstance, NAME_PROPERTY).orElse("");
    }
}
