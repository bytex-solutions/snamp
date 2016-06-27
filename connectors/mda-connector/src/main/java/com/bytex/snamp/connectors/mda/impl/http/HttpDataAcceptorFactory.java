package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mda.impl.MDAConnectorDescriptionProvider;

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

    @Override
    public HttpDataAcceptor create(final String resourceName,
                                           String servletContext,
                                           final Map<String, String> parameters) {
        if (isNullOrEmpty(servletContext))
            servletContext = getServletContext(resourceName);
        return new HttpDataAcceptor(resourceName,
                servletContext,
                () -> MDAConnectorDescriptionProvider.getInstance().parseThreadPool(parameters));
    }

    @Override
    public boolean canCreateFrom(final String connectionString) {
        return isNullOrEmpty(connectionString) || !connectionString.contains(":/");
    }
}
