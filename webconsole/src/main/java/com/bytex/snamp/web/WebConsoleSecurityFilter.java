package com.bytex.snamp.web;

import com.bytex.snamp.security.web.JWTokenLocation;
import com.bytex.snamp.security.web.WebSecurityFilter;

/**
 * Represents security filter for all WebConsole services.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class WebConsoleSecurityFilter extends WebSecurityFilter {
    private WebConsoleSecurityFilter(final JWTokenLocation... tokenLocations) {
        super(tokenLocations);
    }

    static WebConsoleSecurityFilter forWebSocket(){
        return new WebConsoleSecurityFilter(JWTokenLocation.COOKIE, JWTokenLocation.AUTHORIZATION_HEADER);
    }

    static WebConsoleSecurityFilter forRestAPI(){
        return new WebConsoleSecurityFilter(JWTokenLocation.AUTHORIZATION_HEADER);
    }
}
