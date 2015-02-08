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
final class SuggestAdapterParameterValuesOperation extends AbstractSnampComponentParamValueOperation {

    private static final String NAME = "suggestAdapterParameterValuesOperation";

    private static final OpenMBeanParameterInfo ADAPTER_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING
    );

    protected final AbstractSnampManager snampManager;

    SuggestAdapterParameterValuesOperation(final AbstractSnampManager snampManager) throws OpenDataException {
        super(NAME, new ArrayType<String[]>(SimpleType.STRING, true), ADAPTER_NAME_PARAM, PARAM_NAME_PARAM, CONNECTION_STRING_PARAM, LOCALE_PARAM);
        this.snampManager = Objects.requireNonNull(snampManager);
    }

    @Override
    public String[] invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME_PARAM.getName(), String.class, arguments);
        final String parameterName = getArgument(PARAM_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final Map<String, String> tabularData =
                transformTabularDataToMap(getArgument(CONNECTION_STRING_PARAM.getName(), TabularData.class, arguments));

        final SnampComponentDescriptor adapter = getResourceAdapter(snampManager, adapterName);
        if (adapter == null) throw new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName));

        return getSnampCompenentSuggestedValue(adapter, parameterName, locale,
                AgentConfiguration.ResourceAdapterConfiguration.class, tabularData);
    }
}
