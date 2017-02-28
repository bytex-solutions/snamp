package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.TypedEntityConfiguration;
import com.bytex.snamp.management.http.model.AbstractTypedDataObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractTypedEntityConfigurationService<E extends TypedEntityConfiguration, DTO extends AbstractTypedDataObject<E>> extends AbstractEntityConfigurationService<E, DTO> {
    AbstractTypedEntityConfigurationService(final Class<E> entityType) {
        super(entityType);
    }

    /**
     * Change gateway type response.
     *
     * @param name    the name
     * @param newType the new type
     * @return the response
     */
    @PUT
    @Path("/{name}/type")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response changeEntityType(@PathParam("name") final String name, final String newType) {
        return setConfigurationByName(name, config -> config.setType(newType));
    }
}
