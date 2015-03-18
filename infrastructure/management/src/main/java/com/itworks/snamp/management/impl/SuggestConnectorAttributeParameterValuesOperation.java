package com.itworks.snamp.management.impl;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;

/**
 * The type Suggest connector attribute parameter values operation.
 * @author Evgeniy Kirichenko
 */
final class SuggestConnectorAttributeParameterValuesOperation extends AbstractSnampComponentParamValueOperation {
    private static final String NAME = "suggestConnectorAttributeParameterValues";

    /**
     * Instantiates a new Suggest connector attribute parameter values operation.
     *
     * @param snampManager the snamp manager
     * @throws OpenDataException the open data exception
     */
    SuggestConnectorAttributeParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(snampManager, NAME, CONNECTOR_NAME_PARAM, PARAM_NAME_PARAM, CONNECTION_STRING_PARAM, LOCALE_PARAM);
    }

    @Override
    public String[] invoke(final Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM.getName(), String.class, arguments);
        final String parameterName = getArgument(PARAM_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final Map<String, String> tabularData =
                transformTabularDataToMap(getArgument(CONNECTION_STRING_PARAM.getName(), TabularData.class, arguments));

        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);
        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getSnampComponentSuggestedValue(connector, parameterName, locale,
                AgentConfiguration.ManagedResourceConfiguration.AttributeConfiguration.class, tabularData);
    }
}
