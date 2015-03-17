package com.itworks.snamp.management.impl;

import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;


/**
 * The type Suggest adapter parameter values operation.
 * @author Evgeniy Kirichenko
 */
final class SuggestAdapterParameterValuesOperation extends AbstractSnampComponentParamValueOperation {

    private static final String NAME = "suggestAdapterParameterValuesOperation";

    private static final OpenMBeanParameterInfo ADAPTER_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING
    );

    SuggestAdapterParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(snampManager, NAME, ADAPTER_NAME_PARAM, PARAM_NAME_PARAM, CONNECTION_STRING_PARAM, LOCALE_PARAM);
    }

    @Override
    public String[] invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM.getName(), String.class, arguments);
        final String parameterName = getArgument(PARAM_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final Map<String, String> tabularData =
                transformTabularDataToMap(getArgument(CONNECTION_STRING_PARAM.getName(), TabularData.class, arguments));

        final SnampComponentDescriptor adapter = snampManager.getResourceAdapter(adapterName);
        if (adapter == null) throw new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName));
        else return getSnampComponentSuggestedValue(adapter, parameterName, locale,
                AgentConfiguration.ResourceAdapterConfiguration.class, tabularData);
    }
}
