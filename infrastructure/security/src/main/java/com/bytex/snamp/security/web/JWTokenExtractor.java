package com.bytex.snamp.security.web;

import com.sun.jersey.api.core.HttpRequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Used to extract JWT from HTTP request.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
interface JWTokenExtractor {
    Optional<String> extract(final HttpRequestContext request);
    Optional<String> extract(final HttpServletRequest request);
}
