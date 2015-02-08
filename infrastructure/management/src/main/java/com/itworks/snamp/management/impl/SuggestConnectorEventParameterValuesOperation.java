package com.itworks.snamp.management.impl;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.Objects;

/**
 * Created by temni on 2/8/2015.
 */
public class SuggestConnectorEventParameterValuesOperation extends AbstractSnampComponentParamValueOperation{
    private static final String NAME = "suggestConnectorEventParameterValues";

    private static final OpenMBeanParameterInfo CONNECTOR_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "connectorName",
            "The name of the connector",
            SimpleType.STRING
    );

    protected final AbstractSnampManager snampManager;

    SuggestConnectorEventParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(NAME, new ArrayType<String[]>(SimpleType.STRING, true), CONNECTOR_NAME_PARAM, PARAM_NAME_PARAM, LOCALE_PARAM);
        this.snampManager = Objects.requireNonNull(snampManager);
    }

    @Override
    public String[] invoke(Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM.getName(), String.class, arguments);
        final String parameterName = getArgument(PARAM_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);

        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));

        return getSnampCompenentSuggestedValue(connector, parameterName, locale, AgentConfiguration.ManagedResourceConfiguration.EventConfiguration.class);
    }
}
