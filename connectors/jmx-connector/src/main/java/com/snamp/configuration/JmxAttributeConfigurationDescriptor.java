package com.snamp.configuration;

import com.snamp.internal.ReflectionUtils;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JmxAttributeConfigurationDescriptor extends ResourceBasedConfigurationEntityDescription<AttributeConfiguration> {
    private static final String RESOURCE_NAME = "JmxAttributeConfig";
    public static final String OBJECT_NAME_PROPERTY = "objectName";

    public JmxAttributeConfigurationDescriptor(){
        super(AttributeConfiguration.class, RESOURCE_NAME, OBJECT_NAME_PROPERTY);
        final Object o = getClass().getResourceAsStream("JmxAttributeConfig.properties");
    }

    /**
     * Retrieves resource accessor for the specified locale.
     *
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @return The resource accessor.
     */
    @Override
    protected final ResourceBundle getBundle(final Locale loc) {
        return loc != null ? ResourceBundle.getBundle(getResourceName(RESOURCE_NAME), loc) :
                ResourceBundle.getBundle(getResourceName(RESOURCE_NAME));
    }
}
