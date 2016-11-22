package com.bytex.snamp.connector.zipkin;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ZipkinServlet extends ServletContainer {
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
    public void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
        super.doFilter(request, response, chain);
    }
}
