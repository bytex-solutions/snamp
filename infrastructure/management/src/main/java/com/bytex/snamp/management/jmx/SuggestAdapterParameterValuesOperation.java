package com.bytex.snamp.management.jmx;

import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.OpenDataException;
import java.util.Map;


/**
 * The type Suggest adapter parameter values operation.
 * @author Evgeniy Kirichenko
 */
final class SuggestAdapterParameterValuesOperation extends AbstractSnampComponentParamValueOperation {

    private static final String NAME = "suggestAdapterParameterValuesOperation";

    /**
     * Instantiates a new Suggest adapter parameter values operation.
     *
     * @param snampManager the snamp manager
     * @throws OpenDataException the open data exception
     */
    SuggestAdapterParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(snampManager, NAME, ADAPTER_NAME_PARAM, PARAM_NAME_PARAM, CONNECTION_STRING_PARAM, LOCALE_PARAM);
    }

    @Override
    public String[] invoke(final Map<String, ?> arguments) throws Exception {
        final String adapterName = ADAPTER_NAME_PARAM.getArgument(arguments);
        final String parameterName = PARAM_NAME_PARAM.getArgument(arguments);
        final String locale = LOCALE_PARAM.getArgument(arguments);
        final Map<String, String> tabularData =
                MonitoringUtils.transformTabularDataToMap(CONNECTION_STRING_PARAM.getArgument(arguments));

        final SnampComponentDescriptor adapter = snampManager.getResourceAdapter(adapterName);
        if (adapter == null) throw new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName));
        else return getSnampComponentSuggestedValue(adapter, parameterName, locale,
                AgentConfiguration.ResourceAdapterConfiguration.class, tabularData);
    }
}
