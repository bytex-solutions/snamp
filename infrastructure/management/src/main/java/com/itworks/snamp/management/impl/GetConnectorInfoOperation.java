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
final class GetConnectorInfoOperation extends AbstractComponentInfo{
    private static final String NAME = "getConnectorInfo";

    protected final AbstractSnampManager snampManager;

    protected static final OpenMBeanParameterInfo CONNECTOR_NAME = new OpenMBeanParameterInfoSupport(
            "connectorName",
            "Snamp connector name",
            SimpleType.STRING);

    protected GetConnectorInfoOperation(final AbstractSnampManager snampManager) {
        super(NAME, CONNECTOR_NAME, LOCALE_PARAM);
        this.snampManager = snampManager;
    }

    @Override
    public CompositeData invoke(Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final SnampComponentDescriptor connector = getResourceConnector(snampManager, connectorName);
        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));

        return getSnampComponentInfo(connector, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale));
    }
}
