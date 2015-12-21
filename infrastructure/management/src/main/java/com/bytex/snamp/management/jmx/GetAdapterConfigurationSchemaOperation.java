package com.bytex.snamp.management.jmx;

import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.*;
import java.util.Map;


/**
 * The type Get adapter configuration schema operation.
 * @author Evgeniy Kirichenko
 */
final class GetAdapterConfigurationSchemaOperation extends ConfigurationSchemaOperation {
    private static final String NAME = "getAdapterConfigurationSchema";

    /**
     * Instantiates a new Get adapter configuration schema operation.
     *
     * @param manager the manager
     */
    GetAdapterConfigurationSchemaOperation(final AbstractSnampManager manager) {
        super(manager, NAME, ADAPTER_NAME_PARAM, LOCALE_PARAM);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws OpenDataException {
        return getAdapterConfigurationSchema(
                ADAPTER_NAME_PARAM.getArgument(arguments),
                LOCALE_PARAM.getArgument(arguments)
        );
    }

    private CompositeData getAdapterConfigurationSchema(final String adapterName,
                                                        final String locale) throws OpenDataException {
        final SnampComponentDescriptor adapter = snampManager.getResourceAdapter(adapterName);
        return adapter != null ? getConfigurationSchema(adapter, locale) : null;
    }
}
