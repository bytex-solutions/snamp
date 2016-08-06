package com.bytex.snamp.management.jmx;

import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.Locale;
import java.util.Map;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class GetAdapterInfoOperation extends AbstractComponentInfo {
    private static final String NAME = "getAdapterInfo";

    GetAdapterInfoOperation(final AbstractSnampManager snampManager) {
        super(snampManager, NAME, ADAPTER_NAME_PARAM, LOCALE_PARAM);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws OpenDataException {
        final String adapterName = ADAPTER_NAME_PARAM.getArgument(arguments);
        final String locale = LOCALE_PARAM.getArgument(arguments);
        final SnampComponentDescriptor connector = snampManager.getResourceAdapter(adapterName);
        if (connector == null) throw new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName));
        else return getSnampComponentInfo(connector, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale));
    }
}