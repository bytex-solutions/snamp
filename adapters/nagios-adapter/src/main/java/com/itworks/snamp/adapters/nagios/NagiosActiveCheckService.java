package com.itworks.snamp.adapters.nagios;

import com.itworks.snamp.adapters.AbstractAttributesModel;
import com.sun.jersey.spi.resource.Singleton;

import javax.management.MBeanAttributeInfo;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@Path("/")
@Singleton
public final class NagiosActiveCheckService extends AbstractAttributesModel<NagiosAttributeAccessor> {
    private static final String RESOURCE_NAME_PARAM = "resourceName";
    private static final String ATTRIBUTE_NAME_PARAM = "attributeName";

    @Override
    protected NagiosAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
        return new NagiosAttributeAccessor(metadata);
    }

    @Path("/attributes/{" + RESOURCE_NAME_PARAM + "}/{" + ATTRIBUTE_NAME_PARAM + "}")
    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public String getAttribute(@PathParam(RESOURCE_NAME_PARAM)final String resourceName,
                               @PathParam(ATTRIBUTE_NAME_PARAM)final String attributeName) throws WebApplicationException{
        final AttributeRequestProcessor processor = new AttributeRequestProcessor();
        processAttribute(resourceName, attributeName, processor);
        final NagiosPluginOutput result = processor.get();
        if(result == null)
            throw new WebApplicationException(new IllegalArgumentException(String.format("Attribute %s/%s doesn't exist", resourceName, attributeName)),
                    Response.Status.NOT_FOUND);
        else return result.toString();
    }
}
