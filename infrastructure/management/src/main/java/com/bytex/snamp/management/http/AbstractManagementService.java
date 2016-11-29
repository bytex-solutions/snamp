package com.bytex.snamp.management.http;

import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Represents base class for all HTTP-based management API.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractManagementService {
    AbstractManagementService(){

    }

    static WebApplicationException notFound(){
        return new WebApplicationException(Response.Status.NOT_FOUND);
    }

    protected final BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }
}
