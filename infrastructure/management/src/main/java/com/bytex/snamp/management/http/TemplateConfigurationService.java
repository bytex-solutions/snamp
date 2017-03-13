package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.management.http.model.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Map;

/**
 * The type Template configuration service.
 *
 * @param <E>   the type parameter
 * @param <DTO> the type parameter
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class TemplateConfigurationService<E extends ManagedResourceTemplate, DTO extends TemplateDataObject<E>> extends AbstractTypedEntityConfigurationService<E, DTO> {


    /**
     * Instantiates a new Template configuration service.
     *
     * @param entityType the entity type
     */
    TemplateConfigurationService(final Class<E> entityType){
        super(entityType);
    }

    private <F extends FeatureConfiguration> Response setFeature(final String holderName,
                                                                 final String featureName,
                                                                 final Class<F> featureType,
                                                                 final AbstractFeatureDataObject<F> dto,
                                                                 final SecurityContext security) {
        return changingActions(getBundleContext(), security, config -> {
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
                                                                  final Map<String, ? extends AbstractFeatureDataObject<F>> dto,
                                                                  final SecurityContext security){
        return changingActions(getBundleContext(), security, currentConfig -> {
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

    /**
     * Delete attribute response.
     *
     * @param name        the name
     * @param featureName the feature name
     * @return the response
     */
    @DELETE
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE + "/{featureName}")
    public final Response deleteAttribute(@PathParam("name") final String name,
                                          @PathParam("featureName") final String featureName,
                                          @Context final SecurityContext security){
        return FeatureType.ATTRIBUTES.removeFeature(getBundleContext(), security, name, entityType, featureName);
    }

    /**
     * Delete event response.
     *
     * @param name        the name
     * @param featureName the feature name
     * @return the response
     */
    @DELETE
    @Path("/{name}/" + FeatureType.EVENTS_TYPE + "/{featureName}")
    public final Response deleteEvent(@PathParam("name") final String name,
                                        @PathParam("featureName") final String featureName,
                                      @Context final SecurityContext security){
        return FeatureType.EVENTS.removeFeature(getBundleContext(), security, name, entityType, featureName);
    }

    /**
     * Delete operation response.
     *
     * @param name        the name
     * @param featureName the feature name
     * @return the response
     */
    @DELETE
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE + "/{featureName}")
    public final Response deleteOperation(@PathParam("name") final String name,
                                        @PathParam("featureName") final String featureName,
                                          @Context final SecurityContext security){
        return FeatureType.OPERATIONS.removeFeature(getBundleContext(), security, name, entityType, featureName);
    }


    /**
     * Gets attributes.
     *
     * @param name the name
     * @return the attributes
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Map<String, AttributeDataObject> getAttributes(@PathParam("name") final String name) {
        return (Map<String, AttributeDataObject>) FeatureType.ATTRIBUTES.getFeatures(getBundleContext(), name, entityType);
    }

    /**
     * Gets events.
     *
     * @param name the name
     * @return the events
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/{name}/" + FeatureType.EVENTS_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Map<String, EventDataObject> getEvents(@PathParam("name") final String name) {
        return (Map<String, EventDataObject>) FeatureType.EVENTS.getFeatures(getBundleContext(), name, entityType);
    }

    /**
     * Gets operations.
     *
     * @param name the name
     * @return the operations
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Map<String, OperationDataObject> getOperations(@PathParam("name") final String name) {
        return (Map<String, OperationDataObject>) FeatureType.OPERATIONS.getFeatures(getBundleContext(), name, entityType);
    }


    /**
     * Gets attribute.
     *
     * @param name        the name
     * @param featureName the feature name
     * @return the attribute
     */
    @GET
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE + "/{featureName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final AttributeDataObject getAttribute(@PathParam("name") final String name,
                                                        @PathParam("featureName") final String featureName) {
        return (AttributeDataObject) FeatureType.ATTRIBUTES.getFeature(getBundleContext(), name, entityType, featureName)
                .orElseThrow(ResourceConfigurationService::notFound);
    }

    /**
     * Ge event abstract feature data object.
     *
     * @param name        the name
     * @param featureName the feature name
     * @return the abstract feature data object
     */
    @GET
    @Path("/{name}/" + FeatureType.EVENTS_TYPE + "/{featureName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final EventDataObject geEvent(@PathParam("name") final String name,
                                                         @PathParam("featureName") final String featureName) {
        return (EventDataObject) FeatureType.EVENTS.getFeature(getBundleContext(), name, entityType, featureName)
                .orElseThrow(ResourceConfigurationService::notFound);
    }


    /**
     * Gets operation.
     *
     * @param name        the name
     * @param featureName the feature name
     * @return the operation
     */
    @GET
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE + "/{featureName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final OperationDataObject getOperation(@PathParam("name") final String name,
                                                         @PathParam("featureName") final String featureName) {
        return (OperationDataObject) FeatureType.OPERATIONS.getFeature(getBundleContext(), name, entityType, featureName)
                .orElseThrow(ResourceConfigurationService::notFound);
    }


    /**
     * Set certain attribute for specific resource.
     *
     * @param name          the name
     * @param attributeName the attribute name
     * @param dto           the dto
     * @return no content response
     */
    @PUT
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE + "/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setAttribute(@PathParam("name") final String name,
                                             @PathParam("attributeName") final String attributeName,
                                             final AttributeDataObject dto,
                                       @Context final SecurityContext security) {
        return setFeature(name, attributeName, AttributeConfiguration.class, dto, security);
    }

    /**
     * Saves attributes for certain configured resource by its name.
     *
     * @param name the name
     * @param dto  the dto
     * @return no content
     */
    @PUT
    @Path("/{name}/" + FeatureType.ATTRIBUTES_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setAttributes(@PathParam("name") final String name,
                                             final Map<String, AttributeDataObject> dto,
                                        @Context final SecurityContext security) {
        return setFeatures(name, AttributeConfiguration.class, dto, security);
    }


    /**
     * Saves events for certain configured resource by its name.
     *
     * @param name the name
     * @param dto  the dto
     * @return no content
     */
    @PUT
    @Path("/{name}/" + FeatureType.EVENTS_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setEvents(@PathParam("name") final String name,
                                         final Map<String, EventDataObject> dto,
                                    @Context final SecurityContext security) {
        return setFeatures(name, EventConfiguration.class, dto, security);
    }

    /**
     * Set certain event for specific resource.
     *
     * @param name      the name
     * @param eventName the event name
     * @param dto       the dto
     * @return no content response
     */
    @PUT
    @Path("/{name}/" + FeatureType.EVENTS_TYPE + "/{eventName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setEvent(@PathParam("name") final String name,
                                   @PathParam("eventName") final String eventName,
                                   final EventDataObject dto,
                                   @Context final SecurityContext security) {
        return setFeature(name, eventName, EventConfiguration.class, dto, security);
    }

    /**
     * Saves operations for certain configured resource by its name.
     *
     * @param name the name
     * @param dto  the dto
     * @return no content
     */
    @PUT
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE)
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setOperations(@PathParam("name") final String name,
                                             final Map<String, OperationDataObject> dto,
                                        @Context final SecurityContext security) {
        return setFeatures(name, OperationConfiguration.class, dto, security);
    }

    /**
     * Set certain operation for specific resource.
     *
     * @param name          the name
     * @param operationName the operation name
     * @param dto           the dto
     * @return no content response
     */
    @PUT
    @Path("/{name}/" + FeatureType.OPERATIONS_TYPE + "/{operationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public final Response setOperation(@PathParam("name") final String name,
                                       @PathParam("operationName") final String operationName,
                                       final OperationDataObject dto,
                                       @Context final SecurityContext security) {
        return setFeature(name, operationName, OperationConfiguration.class, dto, security);
    }
}
