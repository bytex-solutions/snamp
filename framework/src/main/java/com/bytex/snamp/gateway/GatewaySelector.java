package com.bytex.snamp.gateway;

import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.core.DefaultServiceSelector;
import org.osgi.framework.ServiceReference;

import static com.bytex.snamp.gateway.Gateway.CATEGORY_PROPERTY;
import static com.bytex.snamp.gateway.Gateway.TYPE_CAPABILITY_ATTRIBUTE;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder of OSGi filter used to query instances of {@link Gateway}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class GatewaySelector extends DefaultServiceSelector {
    private static final String CATEGORY = "gateway";
    private static final String NAME_PROPERTY = "instanceName";
    private static final long serialVersionUID = -7919619948002475215L;

    GatewaySelector(){
        put(CATEGORY_PROPERTY, CATEGORY);
    }

    GatewaySelector(final GatewayConfiguration configuration) {
        super(configuration);
        setGatewayType(configuration.getType()).put(CATEGORY_PROPERTY, CATEGORY);
    }

    public GatewaySelector setInstanceName(final String value) {
        if (isNullOrEmpty(value))
            remove(NAME_PROPERTY);
        else
            put(NAME_PROPERTY, value);
        return this;
    }

    public GatewaySelector setGatewayType(final String value){
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
