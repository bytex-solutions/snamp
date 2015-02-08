package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;

/**
 * Created by temni on 2/8/2015.
 */
final class GetAdapterConfigurationSchemaOperation extends ConfigurationSchemaOperation {
    private static final String NAME = "getAdapterConfigurationSchemaOperation";
    private static final OpenMBeanParameterInfo ADAPTER_NAME_PARAM = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "The name of the managed resource adapter",
            SimpleType.STRING
    );
    private static final OpenMBeanParameterInfo LOCALE_PARAM = new OpenMBeanParameterInfoSupport(
            "locale",
            "The expected localization of the configuration schema",
            SimpleType.STRING);

    GetAdapterConfigurationSchemaOperation(final AbstractSnampManager manager) {
        super(manager, NAME, ADAPTER_NAME_PARAM, LOCALE_PARAM);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws OpenDataException {
        return getAdapterConfigurationSchema(
                getArgument(ADAPTER_NAME_PARAM.getName(), String.class, arguments),
                getArgument(LOCALE_PARAM.getName(), String.class, arguments)
        );
    }

    private CompositeData getAdapterConfigurationSchema(final String adapterName,
                                                        final String locale) throws OpenDataException {
        final SnampComponentDescriptor adapter = snampManager.getResourceAdapter(adapterName);
        return adapter != null ? getConfigurationSchema(adapter, locale) : null;
    }
}
