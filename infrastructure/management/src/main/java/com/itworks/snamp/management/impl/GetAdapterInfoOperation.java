package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.AbstractSnampManager;
import com.itworks.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;
import java.util.Locale;
import java.util.Map;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 * @date 09.02.2015
 */
final class GetAdapterInfoOperation extends AbstractComponentInfo {
    private static final String NAME = "getConnectorInfo";

    protected final AbstractSnampManager snampManager;

    protected static final OpenMBeanParameterInfo ADAPTER_NAME = new OpenMBeanParameterInfoSupport(
            "adapterName",
            "Snamp adapter name",
            SimpleType.STRING);

    protected GetAdapterInfoOperation(final AbstractSnampManager snampManager) {
        super(NAME, ADAPTER_NAME, LOCALE_PARAM);
        this.snampManager = snampManager;
    }

    @Override
    public CompositeData invoke(Map<String, ?> arguments) throws Exception {
        final String adapterName = getArgument(ADAPTER_NAME.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final SnampComponentDescriptor connector = getResourceAdapter(snampManager, adapterName);
        if (connector == null) throw new IllegalArgumentException(String.format("Adapter %s doesn't exist", adapterName));

        return getSnampComponentInfo(connector, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale));
    }
}
