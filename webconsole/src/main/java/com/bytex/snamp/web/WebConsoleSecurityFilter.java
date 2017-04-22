package com.bytex.snamp.web;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.web.JWTokenLocation;
import com.bytex.snamp.security.web.WebSecurityFilter;

/**
 * Represents security filter for all WebConsole services.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleSecurityFilter extends WebSecurityFilter {
    private WebConsoleSecurityFilter(final ClusterMember clusterMember, final JWTokenLocation... tokenLocations) {
        super(clusterMember, tokenLocations);
    }

    static WebConsoleSecurityFilter forWebSocket(final ClusterMember clusterMember){
        return new WebConsoleSecurityFilter(clusterMember, JWTokenLocation.COOKIE, JWTokenLocation.AUTHORIZATION_HEADER);
    }

    static WebConsoleSecurityFilter forRestAPI(final ClusterMember clusterMember){
        return new WebConsoleSecurityFilter(clusterMember, JWTokenLocation.AUTHORIZATION_HEADER);
    }
}
