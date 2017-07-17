package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.Convert;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Application;
import java.util.Optional;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ZipkinServlet extends ServletContainer implements ServletSupportService {
    static final String CONTEXT = "/zipkin/api";
    private static final long serialVersionUID = -7869182687331056887L;

    ZipkinServlet(){
        super(createAppConfig());
    }

    private static Application createAppConfig(){
        final DefaultResourceConfig result = new DefaultResourceConfig();
        result.getSingletons().add(new ZipkinHttpService());
        result.getContainerRequestFilters().add(new GZIPContentEncodingFilter());
        return result;
    }

    @Override
    public <T> Optional<T> queryObject(@Nonnull final Class<T> objectType) {
        return Convert.toType(this, objectType);
    }
}
