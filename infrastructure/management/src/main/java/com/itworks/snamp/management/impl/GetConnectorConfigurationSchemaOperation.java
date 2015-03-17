package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;

/**
 * The type Get connector configuration schema operation.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class GetConnectorConfigurationSchemaOperation extends ConfigurationSchemaOperation {
    private static final String NAME = "getConnectorConfigurationSchema";

    /**
     * Instantiates a new Get connector configuration schema operation.
     *
     * @param manager the manager
     */
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
