package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.management.http.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class TemplateConfigurationService<E extends ManagedResourceTemplate, DTO extends TemplateDataObject<E>> extends AbstractEntityConfigurationService<E, DTO> {


    TemplateConfigurationService(final Class<E> entityType){
        super(entityType);
    }

    private <F extends FeatureConfiguration> Response setFeature(final String holderName,
                                                                        final String featureName,
                                                                        final Class<F> featureType,
                                                                        final AbstractFeatureDataObject<F> dto) {
        return changingActions(getBundleContext(), config -> {
            final ManagedResourceTemplate resource = config.getEntities(entityType).get(holderName);
            if (resource != null) {
                dto.exportTo(resource.getFeatures(featureType).getOrAdd(featureName));
                return true;
            } else
                throw notFound();
        });
    }

    private <F extends FeatureConfiguration> Response setFeatures(final String holderName,
                                                                  final Class<F> featureType,
                                                                  final Map<String, ? extends AbstractFeatureDataObject<F>> dto){
        return changingActions(getBundleContext(), currentConfig -> {
            final ManagedResourceTemplate mrc =
                    currentConfig.getEntities(entityType).get(holderName);
            if (mrc != null) {
                final EntityMap<? extends F> em = mrc.getFeatures(featureType);
                em.clear();
                dto.entrySet().forEach(entry -> entry.getValue().exportTo(em.getOrAdd(entry.getKey())));
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    @DELETE
    @Path("/{name}/{feature}/{featureName}")
    public final Response deleteFeature(@PathParam("name") final String name,
                                        @PathParam("feature") final FeatureType type,
                                        @PathParam("featureName") final String featureName){
        return type.removeFeature(getBundleContext(), name, entityType, featureName);
    }

    /**
     * Returns attributes for certain configured resource by its name.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/{feature}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Map<String, ? extends AbstractFeatureDataObject<?>> getFeatures(@PathParam("name") final String name, @PathParam("feature") final FeatureType type) {
        return type.getFeatures(getBundleContext(), name, entityType);
    }

    /**
     * Returns certain attribute for specific resource.
     *
     * @return AbstractDTOEntity that contains attributes configuration (or null)
     */
    @GET
    @Path("/{name}/{feature}/{featureName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final AbstractFeatureDataObject<?> getFeature(@PathParam("name") final String name,
                                                @PathParam("feature") final FeatureType type,
                                                        @PathParam("featureName") final String featureName) {
        return type.getFeature(getBundleContext(), name, entityType, featureName).orElseThrow(ResourceConfigurationService::notFound);
    }

    /**
     * Set certain attribute for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE + "/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setAttribute(@PathParam("name") final String name,
                                             @PathParam("attributeName") final String attributeName,
                                             final AttributeDataObject dto) {
        return setFeature(name, attributeName, AttributeConfiguration.class, dto);
    }

    /**
     * Saves attributes for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setAttributes(@PathParam("name") final String name,
                                             final Map<String, AttributeDataObject> dto) {
        return setFeatures(name, AttributeConfiguration.class, dto);
    }



    /**
     * Saves events for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/" + FeatureType.EVENTS_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setEvents(@PathParam("name") final String name,
                                         final Map<String, EventDataObject> dto) {
        return setFeatures(name, EventConfiguration.class, dto);
    }

    /**
     * Set certain event for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/" + FeatureType.EVENTS_TYPE + "/{eventName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setEvent(@PathParam("name") final String name,
                                   @PathParam("eventName") final String eventName,
                                   final EventDataObject dto) {
        return setFeature(name, eventName, EventConfiguration.class, dto);
    }

    /**
     * Saves operations for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setOperations(@PathParam("name") final String name,
                                             final Map<String, OperationDataObject> dto) {
        return setFeatures(name, OperationConfiguration.class, dto);
    }

    /**
     * Set certain operation for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE + "/{operationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setOperation(@PathParam("name") final String name,
                                       @PathParam("operationName") final String operationName,
                                       final OperationDataObject dto) {
        return setFeature(name, operationName, OperationConfiguration.class, dto);
    }
}
