package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mda.impl.MDAConnectorConfigurationParser;

import java.util.Map;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents factory of {@link HttpDataAcceptor} class.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class HttpDataAcceptorFactory implements DataAcceptorFactory {
    private static final String SERVLET_CONTEXT = "/snamp/connectors/mda/";

    private static String getServletContext(final String resourceName){
        return SERVLET_CONTEXT.concat(resourceName);
    }

    private static HttpDataAcceptor create(final String resourceName,
                                           String servletContext,
                                           final Map<String, String> parameters,
                                           final MDAConnectorConfigurationParser configurationParser) {
        if (isNullOrEmpty(servletContext))
            servletContext = getServletContext(resourceName);
        return new HttpDataAcceptor(resourceName,
                servletContext,
                () -> configurationParser.getThreadPool(parameters));
    }

    @Override
    public HttpDataAcceptor create(final String resourceName,
                                   final String servletContext,
                                   final Map<String, String> parameters) throws Exception {
        return create(resourceName, servletContext, parameters, new MDAConnectorConfigurationParser());
    }

    @Override
    public boolean canCreateFrom(final String connectionString) {
        return isNullOrEmpty(connectionString) || !connectionString.contains(":/");
    }
}
