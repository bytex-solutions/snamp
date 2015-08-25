package com.bytex.snamp.connectors.mda.http;

import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mda.MdaThreadPoolConfig;
import com.google.common.base.Strings;

import java.util.Map;

/**
 * Represents factory of {@link HttpDataAcceptor} class.
 * @author Roman Sakno
 * @version 1.0
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
                               final Map<String, String> parameters) throws Exception {
        if(Strings.isNullOrEmpty(servletContext))
            servletContext = getServletContext(resourceName);
        return new HttpDataAcceptor(resourceName, servletContext, new MdaThreadPoolConfig(resourceName, parameters));
    }

    @Override
    public boolean canCreateFrom(final String connectionString) {
        return Strings.isNullOrEmpty(connectionString) || !connectionString.contains(":/");
    }
}
