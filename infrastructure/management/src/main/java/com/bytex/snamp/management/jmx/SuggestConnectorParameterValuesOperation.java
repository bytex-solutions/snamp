package com.bytex.snamp.management.jmx;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;


/**
 * The type Suggest connector parameter values operation.
 * @author Evgeniy Kirichenko
 */
final class SuggestConnectorParameterValuesOperation extends AbstractSnampComponentParamValueOperation {
    private static final String NAME = "suggestConnectorParameterValues";

    /**
     * Instantiates a new Suggest connector parameter values operation.
     *
     * @param snampManager the snamp manager
     * @throws OpenDataException the open data exception
     */
    SuggestConnectorParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(snampManager, NAME, CONNECTOR_NAME_PARAM, PARAM_NAME_PARAM, CONNECTION_STRING_PARAM, LOCALE_PARAM);
    }

    @Override
    public String[] invoke(Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM.getName(), String.class, arguments);
        final String parameterName = getArgument(PARAM_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final Map<String, String> tabularData =
                MonitoringUtils.transformTabularDataToMap(getArgument(CONNECTION_STRING_PARAM.getName(), TabularData.class, arguments));

        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);
        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getSnampComponentSuggestedValue(connector, parameterName, locale,
                AgentConfiguration.ManagedResourceConfiguration.class, tabularData);
    }
}
