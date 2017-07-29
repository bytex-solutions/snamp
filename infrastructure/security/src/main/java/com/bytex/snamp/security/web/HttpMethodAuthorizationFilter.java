package com.bytex.snamp.security.web;

import com.bytex.snamp.security.RBAC;
import com.bytex.snamp.security.Role;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import java.util.Arrays;

/**
 * Authorizes HTTP request using its HTTP method.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class HttpMethodAuthorizationFilter implements ContainerRequestFilter {
    //key - HTTP method, values - allowed roles.
    private final Multimap<String, Role> allowedMethods;

    public HttpMethodAuthorizationFilter(){
        allowedMethods = HashMultimap.create();
    }

    public final void addRestriction(final String method, final Role... roles) {
        allowedMethods.putAll(method, Arrays.asList(roles));
    }

    protected boolean authorizationRequired(final HttpRequestContext request){
        return true;
    }

    /**
     * Filter the request.
     * <p>
     * An implementation may modify the state of the request or
     * create a new instance.
     *
     * @param request the request.
     * @return the request.
     */
    @Override
    public final ContainerRequest filter(final ContainerRequest request) {
        if(authorizationRequired(request) && allowedMethods.containsKey(request.getMethod()))
            RBAC.authorize(request.getSecurityContext(), allowedMethods.get(request.getMethod()));
        return request;
    }
}
