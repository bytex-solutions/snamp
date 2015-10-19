package com.bytex.snamp.management.jmx;

import com.bytex.snamp.management.AbstractSnampManager;
import com.bytex.snamp.management.SnampComponentDescriptor;

import javax.management.openmbean.CompositeData;
import java.util.Locale;
import java.util.Map;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class GetConnectorInfoOperation extends AbstractComponentInfo{
    private static final String NAME = "getConnectorInfo";

    /**
     * Instantiates a new Get connector info operation.
     *
     * @param snampManager the snamp manager
     */
    GetConnectorInfoOperation(final AbstractSnampManager snampManager) {
        super(snampManager, NAME, CONNECTOR_NAME_PARAM, LOCALE_PARAM);
    }

    @Override
    public CompositeData invoke(final Map<String, ?> arguments) throws Exception {
        final String connectorName = getArgument(CONNECTOR_NAME_PARAM.getName(), String.class, arguments);
        final String locale = getArgument(LOCALE_PARAM.getName(), String.class, arguments);
        final SnampComponentDescriptor connector = snampManager.getResourceConnector(connectorName);
        if (connector == null) throw new IllegalArgumentException(String.format("Connector %s doesn't exist", connectorName));
        else return getSnampComponentInfo(connector, locale == null || locale.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(locale));
    }
}
