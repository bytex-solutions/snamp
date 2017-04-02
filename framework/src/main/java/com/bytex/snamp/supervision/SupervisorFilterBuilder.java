package com.bytex.snamp.supervision;

import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.core.FrameworkService;
import com.bytex.snamp.core.SimpleFilterBuilder;
import org.osgi.framework.ServiceReference;

import static com.bytex.snamp.gateway.Gateway.TYPE_CAPABILITY_ATTRIBUTE;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class SupervisorFilterBuilder extends SimpleFilterBuilder {
    private static final long serialVersionUID = 5989084420590109152L;
    private static final String CATEGORY = "supervisor";
    private static final String GROUP_NAME_PROPERTY = "groupName";

    SupervisorFilterBuilder(){
        put(FrameworkService.CATEGORY_PROPERTY, CATEGORY);
    }

    SupervisorFilterBuilder(final SupervisorConfiguration configuration){
        super(configuration);
        setSupervisorType(configuration.getType()).put(FrameworkService.CATEGORY_PROPERTY, CATEGORY);
    }

    public SupervisorFilterBuilder setGroupName(final String value) {
        if (isNullOrEmpty(value))
            remove(GROUP_NAME_PROPERTY);
        else
            put(GROUP_NAME_PROPERTY, value);
        return this;
    }

    public SupervisorFilterBuilder setSupervisorType(final String value){
        if(isNullOrEmpty(value))
            remove(TYPE_CAPABILITY_ATTRIBUTE);
        else
            put(TYPE_CAPABILITY_ATTRIBUTE, value);
        return this;
    }

    static String getGroupName(final ServiceReference<Supervisor> gatewayInstance) {
        return getReferencePropertyAsString(gatewayInstance, GROUP_NAME_PROPERTY).orElse("");
    }
}