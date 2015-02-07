package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class GetConnectorConfigurationSchemaOperation extends ConfigurationSchemaOperation {
    private static final String NAME = "getConnectorConfigurationSchema";
    private static final OpenMBeanParameterInfo CONNECTOR_NAME_PARAM = new OpenMBeanParameterInfoSupport(
        "connectorName",
        "The name of the managed resource connector",
        SimpleType.STRING
    );
    private static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    GetConnectorConfigurationSchemaOperation(final AbstractSnampManager manager){
        super(manager, NAME, CONNECTOR_NAME_PARAM, LOCALE_PARAM);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws OpenDataException {
        return getConnectorConfigurationSchema(
                getArgument(CONNECTOR_NAME_PARAM.getName(), String.class, arguments),
                getArgument(LOCALE_PARAM.getName(), String.class, arguments)
        );
    }

    private CompositeData getConnectorConfigurationSchema(final String connectorName,
                                                                   final String locale) throws OpenDataException{
        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);
        return connector != null ? getConfigurationSchema(connector, locale) : null;
    }
}
