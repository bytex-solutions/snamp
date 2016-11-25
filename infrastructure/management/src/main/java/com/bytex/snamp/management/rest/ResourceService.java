package com.bytex.snamp.management.rest;

import com.bytex.snamp.configuration.*;
import com.bytex.snamp.management.rest.model.dto.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * Provides API for SNAMP resources management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/resource")
public final class ResourceService extends BaseRestConfigurationService {

    /**
     * Returns all the configured managed resources.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getConfiguration() {
        return DTOFactory.buildManagedResources(readOnlyActions(currentConfig ->
                (EntityMap<? extends ManagedResourceConfiguration>)
                        currentConfig.getEntities(ManagedResourceConfiguration.class)));
    }

    /**
     * Returns configuration for certain configured resource by its name.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ManagedResourceConfigurationDTO getConfigurationByName(@PathParam("name") final String name) {
        return DTOFactory.buildManagedResource(readOnlyActions(configuration -> {
                if (configuration.getEntities(ManagedResourceConfiguration.class).get(name) != null) {
                    return configuration.getEntities(ManagedResourceConfiguration.class).get(name);
                } else {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
                }
        }));
    }

    /**
     * Updated certain resource.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @PUT
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setConfigurationByName(@PathParam("name") final String name,
                                           final ManagedResourceConfigurationDTO object) {
        return changingActions(currentConfig -> {
            final EntityMap<? extends ManagedResourceConfiguration> entityMap =
                    currentConfig.getEntities(ManagedResourceConfiguration.class);
            final ManagedResourceConfiguration mrc = entityMap.getOrAdd(name);
            if (mrc != null) {
                mrc.setParameters(object.getParameters());
                mrc.setConnectionString(object.getConnectionString());
                mrc.setType(object.getType());
                mrc.getFeatures(FeatureConfiguration.class).clear();

                object.getAttributes().entrySet().forEach(entry -> {
                    final AttributeConfiguration configuration = mrc.getFeatures(AttributeConfiguration.class)
                            .getOrAdd(entry.getKey());
                    configuration.setParameters(entry.getValue().getParameters());
                    // http://stackoverflow.com/questions/27952472/serialize-deserialize-java-8-java-time-with-jackson-json-mapper
                    configuration.setReadWriteTimeout(entry.getValue().getReadWriteTimeout());
                });

                object.getEvents().entrySet().forEach(entry -> {
                    final EventConfiguration configuration = mrc.getFeatures(EventConfiguration.class)
                            .getOrAdd(entry.getKey());
                    configuration.setParameters(entry.getValue().getParameters());
                });

                object.getOperations().entrySet().forEach(entry -> {
                    final OperationConfiguration configuration = mrc.getFeatures(OperationConfiguration.class)
                            .getOrAdd(entry.getKey());
                    configuration.setParameters(entry.getValue().getParameters());
                    // http://stackoverflow.com/questions/27952472/serialize-deserialize-java-8-java-time-with-jackson-json-mapper
                    configuration.setInvocationTimeout(entry.getValue().getInvocationTimeout());
                });
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Remove managed resource from configuration by its name
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeConfigurationByName(@PathParam("name") final String name) {
        return changingActions(currentConfig -> {
            if (currentConfig.getEntities(ManagedResourceConfiguration.class).get(name) == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                return currentConfig.getEntities(ManagedResourceConfiguration.class).remove(name) != null;
            }
        });
    }

    /**
     * Returns parameters for certain configured resource by its name.
     *
     * @return Map that contains parameters configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getParametersForResource(@PathParam("name") final String name) {
        return readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                return mrc.getParameters();
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Set parameters for certain configured resource by its name.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/parameters")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setParametersForResource(@PathParam("name") final String name, final Map<String, String> object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                mrc.setParameters(object);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain parameter for certain configured resource by its name.
     *
     * @return String value of the parameter
     */
    @GET
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getParameterByName(@PathParam("name") final String name,
                                     @PathParam("paramName") final String paramName) {
        return readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                return mrc.getParameters().get(paramName);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain parameter for certain configured resource by its name.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setParameterByName(@PathParam("name") final String name,
                                       @PathParam("paramName") final String paramName,
                                       final String value) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                mrc.getParameters().put(paramName, value);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Removes certain parameter for certain configured resource by its name.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeParameterByName(@PathParam("name") final String name,
                                       @PathParam("paramName") final String paramName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                return mrc.getParameters().remove(paramName) != null;
            }
        });
    }

    /**
     * Returns attributes for certain configured resource by its name.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getAttributesForResource(@PathParam("name") final String name) {
        return DTOFactory.buildAttributes(readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                return (EntityMap<? extends AttributeConfiguration>) mrc.getFeatures(AttributeConfiguration.class);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }));
    }

    /**
     * Saves attributes for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/attributes")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttributesForResource(@PathParam("name") final String name,
                                        final Map<String, AttributeDTOEntity> object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                final EntityMap<? extends AttributeConfiguration> em = mrc.getFeatures(AttributeConfiguration.class);
                em.clear();
                object.entrySet().forEach(entry -> {
                    final AttributeConfiguration configuration = em.getOrAdd(entry.getKey());
                    configuration.setParameters(entry.getValue().getParameters());
                    // http://stackoverflow.com/questions/27952472/serialize-deserialize-java-8-java-time-with-jackson-json-mapper
                    configuration.setReadWriteTimeout(entry.getValue().getReadWriteTimeout());
                });
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain attribute for specific resource.
     *
     * @return AbstractDTOEntity that contains attributes configuration (or null)
     */
    @GET
    @Path("/{name}/attributes/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AttributeDTOEntity getAttributeByName(@PathParam("name") final String name,
                                                @PathParam("attributeName") final String attributeName) {
        return DTOFactory.buildAttribute(readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc == null || mrc.getFeatures(AttributeConfiguration.class).isEmpty()
                    || mrc.getFeatures(AttributeConfiguration.class).get(attributeName) == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                return mrc.getFeatures(AttributeConfiguration.class).get(attributeName);
            }
        }));
    }

    /**
     * Set certain attribute for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/attributes/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttributeByName(@PathParam("name") final String name,
                                       @PathParam("attributeName") final String attributeName,
                                       final AttributeDTOEntity object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                mrc.getFeatures(AttributeConfiguration.class).getOrAdd(attributeName)
                        .setParameters(object.getParameters());
                mrc.getFeatures(AttributeConfiguration.class).getOrAdd(attributeName)
                        .setReadWriteTimeout(object.getReadWriteTimeout());
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }


    /**
     * Removes certain attribute for specific resource.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/attributes/{attributeName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttributeParamByName(@PathParam("name") final String name,
                                          @PathParam("attributeName") final String attributeName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc == null || mrc.getFeatures(AttributeConfiguration.class).isEmpty()
                || mrc.getFeatures(AttributeConfiguration.class).get(attributeName) == null) {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            } else {
                return mrc.getFeatures(AttributeConfiguration.class).remove(attributeName) != null;
            }
        });
    }

    /**
     * Set certain param for certain attribute for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/attributes/{attributeName}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttributeParamByName(@PathParam("name") final String name,
                                            @PathParam("attributeName") final String attributeName,
                                            @PathParam("paramName") final String paramName,
                                            final String object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && mrc.getFeatures(AttributeConfiguration.class) != null
                    && mrc.getFeatures(AttributeConfiguration.class).get(attributeName) != null) {
                mrc.getFeatures(AttributeConfiguration.class).get(attributeName).getParameters().put(paramName, object);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }


    /**
     * Set certain param for certain attribute for specific resource.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/attributes/{attributeName}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttributeParamByName(@PathParam("name") final String name,
                                               @PathParam("attributeName") final String attributeName,
                                               @PathParam("paramName") final String paramName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && mrc.getFeatures(AttributeConfiguration.class) != null
                    && mrc.getFeatures(AttributeConfiguration.class).get(attributeName) != null) {
                return mrc.getFeatures(AttributeConfiguration.class).get(attributeName)
                        .getParameters().remove(paramName) != null;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns events for certain configured resource by its name.
     *
     * @return Map that contains events configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/events")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getEventsForResource(@PathParam("name") final String name) {
        return DTOFactory.buildEvents(readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                return (EntityMap<? extends EventConfiguration>) mrc.getFeatures(EventConfiguration.class);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }));
    }

    /**
     * Saves events for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/events")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setEventsForResource(@PathParam("name") final String name,
                                             final Map<String, EventDTOEntity> object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                final EntityMap<? extends EventConfiguration> em = mrc.getFeatures(EventConfiguration.class);
                em.clear();
                object.entrySet().forEach(entry -> {
                    final EventConfiguration configuration = em.getOrAdd(entry.getKey());
                    configuration.setParameters(entry.getValue().getParameters());
                });
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain event for specific resource.
     *
     * @return AbstractDTOEntity that contains event configuration (or null)
     */
    @GET
    @Path("/{name}/events/{eventName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AbstractDTOEntity getEventByName(@PathParam("name") final String name,
                                            @PathParam("eventName") final String eventName) {
        return DTOFactory.buildEvent(readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && !mrc.getFeatures(EventConfiguration.class).isEmpty() &&
                    mrc.getFeatures(AttributeConfiguration.class).get(eventName) != null) {
                return mrc.getFeatures(EventConfiguration.class).get(eventName);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }));
    }

    /**
     * Set certain event for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/events/{eventName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setEventByName(@PathParam("name") final String name,
                                   @PathParam("eventName") final String eventName,
                                   final EventDTOEntity object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                mrc.getFeatures(EventConfiguration.class).getOrAdd(eventName)
                        .setParameters(object.getParameters());
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }


    /**
     * Removes certain event for specific resource.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/events/{eventName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeAttributesByName(@PathParam("name") final String name,
                                           @PathParam("eventName") final String eventName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && !mrc.getFeatures(EventConfiguration.class).isEmpty() &&
                    mrc.getFeatures(EventConfiguration.class).get(eventName) != null) {
                        return mrc.getFeatures(EventConfiguration.class).remove(eventName) != null;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Set certain param for certain event for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/events/{eventName}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setEventParamByName(@PathParam("name") final String name,
                                            @PathParam("eventName") final String eventName,
                                            @PathParam("paramName") final String paramName,
                                            final String object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && mrc.getFeatures(EventConfiguration.class) != null
                    && mrc.getFeatures(EventConfiguration.class).get(eventName) != null) {
                mrc.getFeatures(EventConfiguration.class).get(eventName).getParameters().put(paramName, object);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }


    /**
     * Set certain param for certain event for specific resource.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/events/{eventName}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeEventParamByName(@PathParam("name") final String name,
                                               @PathParam("eventName") final String eventName,
                                               @PathParam("paramName") final String paramName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && mrc.getFeatures(EventConfiguration.class) != null
                    && mrc.getFeatures(EventConfiguration.class).get(eventName) != null) {
                return mrc.getFeatures(EventConfiguration.class).get(eventName)
                        .getParameters().remove(paramName) != null;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns operations for certain configured resource by its name.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/operations")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map getOperationsForResource(@PathParam("name") final String name) {
        return DTOFactory.buildOperations(readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                return (EntityMap<? extends OperationConfiguration>) mrc.getFeatures(OperationConfiguration.class);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }));
    }

    /**
     * Saves operations for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/operations")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setOperationsForResource(@PathParam("name") final String name,
                                         final Map<String, OperationDTOEntity> object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                final EntityMap<? extends OperationConfiguration> em = mrc.getFeatures(OperationConfiguration.class);
                em.clear();
                object.entrySet().forEach(entry -> {
                    final OperationConfiguration configuration = em.getOrAdd(entry.getKey());
                    configuration.setParameters(entry.getValue().getParameters());
                });
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Returns certain operation for specific resource.
     *
     * @return AbstractDTOEntity that contains operation configuration (or null)
     */
    @GET
    @Path("/{name}/operations/{operationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public AbstractDTOEntity getOperationByName(@PathParam("name") final String name,
                                                @PathParam("operationName") final String operationName) {
        return DTOFactory.buildOperation(readOnlyActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && !mrc.getFeatures(OperationConfiguration.class).isEmpty() &&
                    mrc.getFeatures(OperationConfiguration.class).get(operationName) != null) {
                return mrc.getFeatures(OperationConfiguration.class).get(operationName);
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        }));
    }

    /**
     * Set certain operation for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/operations/{operationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setAttributeByName(@PathParam("name") final String name,
                                       @PathParam("operationName") final String operationName,
                                       final OperationDTOEntity object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null) {
                mrc.getFeatures(OperationConfiguration.class).getOrAdd(operationName)
                        .setParameters(object.getParameters());
                mrc.getFeatures(OperationConfiguration.class).getOrAdd(operationName)
                        .setInvocationTimeout(object.getInvocationTimeout());
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }


    /**
     * Removes certain operation for specific resource.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/operations/{operationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeOperationByName(@PathParam("name") final String name,
                                          @PathParam("operationName") final String operationName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && !mrc.getFeatures(OperationConfiguration.class).isEmpty()
                    && mrc.getFeatures(OperationConfiguration.class).get(operationName) != null) {
                return mrc.getFeatures(OperationConfiguration.class).remove(operationName) != null;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }

    /**
     * Set certain param for certain operation for specific resource.
     *
     * @return no content response
     */
    @PUT
    @Path("/{name}/operations/{operationName}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setOperationParamByName(@PathParam("name") final String name,
                                        @PathParam("operationName") final String operationName,
                                        @PathParam("paramName") final String paramName,
                                        final String object) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && mrc.getFeatures(OperationConfiguration.class) != null
                    && mrc.getFeatures(OperationConfiguration.class).get(operationName) != null) {
                mrc.getFeatures(OperationConfiguration.class).get(operationName).getParameters().put(paramName, object);
                return true;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }


    /**
     * Set certain param for certain operation for specific resource.
     *
     * @return no content response
     */
    @DELETE
    @Path("/{name}/operations/{operationName}/parameters/{paramName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeOperationParamByName(@PathParam("name") final String name,
                                           @PathParam("operationName") final String operationName,
                                           @PathParam("paramName") final String paramName) {
        return changingActions(currentConfig -> {
            final ManagedResourceConfiguration mrc =
                    currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
            if (mrc != null && mrc.getFeatures(OperationConfiguration.class) != null
                    && mrc.getFeatures(OperationConfiguration.class).get(operationName) != null) {
                return mrc.getFeatures(OperationConfiguration.class).get(operationName)
                        .getParameters().remove(paramName) != null;
            } else {
                throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
            }
        });
    }
}
