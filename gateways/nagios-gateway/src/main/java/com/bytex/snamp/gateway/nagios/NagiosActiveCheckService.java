package com.bytex.snamp.gateway.nagios;

import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.sun.jersey.spi.resource.Singleton;

import javax.management.MBeanAttributeInfo;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@Singleton
@Path("/")
public final class NagiosActiveCheckService extends ModelOfAttributes<NagiosAttributeAccessor> {

    @Override
    protected NagiosAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new NagiosAttributeAccessor(metadata);
    }

    @Path(NagiosAttributeAccessor.ATTRIBUTE_ACCESS_PATH)
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public String getAttribute(@PathParam(NagiosAttributeAccessor.RESOURCE_URL_PARAM)final String resourceName,
                               @PathParam(NagiosAttributeAccessor.ATTRIBUTE_URL_PARAM)final String attributeName) throws WebApplicationException{
        final AttributeRequestProcessor processor = new AttributeRequestProcessor();
        try {
            processAttribute(resourceName, attributeName, processor);
        } catch (final InterruptedException e) {
            throw new WebApplicationException(e);
        }
        final NagiosPluginOutput result = processor.get();
        if(result == null)
            throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s/%s doesn't exist", resourceName, attributeName)),
                    Response.Status.NOT_FOUND);
        else return result.toString();
    }
}
