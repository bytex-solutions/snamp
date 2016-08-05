package com.bytex.snamp.management.jmx;

import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

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
        final String connectorName = CONNECTOR_NAME_PARAM.getArgument(arguments);
        final String parameterName = PARAM_NAME_PARAM.getArgument(arguments);
        final String locale = LOCALE_PARAM.getArgument(arguments);
        final Map<String, String> tabularData =
                MonitoringUtils.transformTabularDataToMap(CONNECTION_STRING_PARAM.getArgument(arguments));

        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);
        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getSnampComponentSuggestedValue(connector, parameterName, locale,
                ManagedResourceConfiguration.AttributeConfiguration.class, tabularData);
    }
}
