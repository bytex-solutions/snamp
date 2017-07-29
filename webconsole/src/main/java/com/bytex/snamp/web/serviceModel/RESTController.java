package com.bytex.snamp.web.serviceModel;

/**
 * Represents REST service provides support for SNAMP Web Console.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface RESTController extends WebConsoleService {
    /**
     * Gets URL context of the service.
     * @return URL context of the service.
     */
    String getUrlContext();
}
