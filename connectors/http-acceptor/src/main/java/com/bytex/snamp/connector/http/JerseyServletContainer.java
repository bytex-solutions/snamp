package com.bytex.snamp.connector.http;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.ws.rs.core.Application;

/**
 * Represents customized servlet container.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class JerseyServletContainer extends ServletContainer {
    private static final long serialVersionUID = 5710139261115306229L;

    JerseyServletContainer(){
        super(createAppConfig());
    }

    private static Application createAppConfig(){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new AcceptorService());
        //result.getProviderClasses().add(JacksonJsonProvider.class);
        return result;
    }
}
