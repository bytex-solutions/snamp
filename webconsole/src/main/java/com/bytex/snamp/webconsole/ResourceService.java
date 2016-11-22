package com.bytex.snamp.webconsole;

import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.webconsole.model.dto.*;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

/**
 * Provides API for SNAMP configuration management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/resource")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public final class ResourceService {
    /**
     * Returns all the configured managed resources.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    public Map getConfiguration() throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        final Box<EntityMap<? extends ManagedResourceConfiguration>> container = BoxFactory.create(null);
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                container.set(currentConfig.getEntities(ManagedResourceConfiguration.class));
            });
        } finally {
            admin.release(bc);
        }
        return DTOFactory.build(container.get());
    }

    /**
     * Returns configuration for certain configured resource by its name.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}")
    public AbstractDTOClass getConfigurationByName(@PathParam("name") final String name) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        final Box<ManagedResourceConfiguration> container = BoxFactory.create(null);
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                container.set(currentConfig.getEntities(ManagedResourceConfiguration.class).get(name));
            });
        } finally {
            admin.release(bc);
        }
        return DTOFactory.build(container.get());
    }

    /**
     * Updated certain resource.
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @PUT
    @Path("/{name}")
    public Response setConfigurationByName(@PathParam("name") final String name,
                                           final ManagedResourceConfigurationDTO object) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            //verify first and second resources
            admin.get().processConfiguration(currentConfig -> {
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
                    return false;
                }
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }

    /**
     * Remove managed resource from configuration by its name
     *
     * @return Map that contains configuration (or empty map if no resources are configured)
     */
    @DELETE
    @Path("/{name}")
    public Response removeConfigurationByName(@PathParam("name") final String name) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            // if nothing was removed - no modification should be commited
            admin.get().processConfiguration(currentConfig ->
                    currentConfig.getEntities(ManagedResourceConfiguration.class).remove(name) != null
            );
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }

    /**
     * Returns attributes for certain configured resource by its name.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/attributes")
    public Map getAttributesForResource(@PathParam("name") final String name) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        final Box<EntityMap<? extends AttributeConfiguration>> container = BoxFactory.create(null);
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                final ManagedResourceConfiguration mrc =
                        currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
                if (mrc != null) {
                    container.set(mrc.getFeatures(AttributeConfiguration.class));
                }
            });
        } finally {
            admin.release(bc);
        }
        return DTOFactory.buildAttributes(container.get());
    }

    /**
     * Saves attributes for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/attributes")
    public Response setAttributesForResource(@PathParam("name") final String name,
                                        final Map<String, AttributeDTOEntity> object) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            //verify first and second resources
            admin.get().processConfiguration(currentConfig -> {
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
                    return false;
                }
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }

    /**
     * Returns certain attribute for specific resource.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/attributes/{attributeName}")
    public AbstractDTOEntity getAttributesByName(@PathParam("name") final String name,
                                                 @PathParam("attributeName") final String attributeName) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        final Box<AttributeConfiguration> container = BoxFactory.create(null);
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                final ManagedResourceConfiguration mrc =
                        currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
                if (mrc != null && !mrc.getFeatures(AttributeConfiguration.class).isEmpty() &&
                        mrc.getFeatures(AttributeConfiguration.class).get(attributeName) != null) {
                    container.set(mrc.getFeatures(AttributeConfiguration.class).get(attributeName));
                }
            });
        } finally {
            admin.release(bc);
        }
        return DTOFactory.build(container.get());
    }

    /**
     * Set certain attribute for specific resource.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @PUT
    @Path("/{name}/attributes/{attributeName}")
    public Response getAttributesByName(@PathParam("name") final String name,
                                        @PathParam("attributeName") final String attributeName,
                                        final AttributeDTOEntity object) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            //verify first and second resources
            admin.get().processConfiguration(currentConfig -> {
                final ManagedResourceConfiguration mrc =
                        currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
                if (mrc != null) {
                    mrc.getFeatures(AttributeConfiguration.class).getOrAdd(attributeName)
                            .setParameters(object.getParameters());
                    mrc.getFeatures(AttributeConfiguration.class).getOrAdd(attributeName)
                            .setReadWriteTimeout(object.getReadWriteTimeout());
                    return true;
                } else {
                    return false;
                }
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }


    /**
     * Returns certain attribute for specific resource.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @DELETE
    @Path("/{name}/attributes/{attributeName}")
    public Response removetAttributesByName(@PathParam("name") final String name,
                                            @PathParam("attributeName") final String attributeName) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            //verify first and second resources
            admin.get().processConfiguration(currentConfig -> {
                final ManagedResourceConfiguration mrc =
                        currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
                return mrc != null && !mrc.getFeatures(AttributeConfiguration.class).isEmpty()
                        && mrc.getFeatures(AttributeConfiguration.class).remove(attributeName) != null;
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }

    /**
     * Returns events for certain configured resource by its name.
     *
     * @return Map that contains events configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/events")
    public Map getEventsForResource(@PathParam("name") final String name) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        final Box<EntityMap<? extends EventConfiguration>> container = BoxFactory.create(null);
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                final ManagedResourceConfiguration mrc =
                        currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
                if (mrc != null) {
                    container.set(mrc.getFeatures(EventConfiguration.class));
                }
            });
        } finally {
            admin.release(bc);
        }
        return DTOFactory.buildEvents(container.get());
    }

    /**
     * Saves events for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/events")
    public Response setEventsForResource(@PathParam("name") final String name,
                                             final Map<String, EventDTOEntity> object) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            //verify first and second resources
            admin.get().processConfiguration(currentConfig -> {
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
                    return false;
                }
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }

    /**
     * Returns operations for certain configured resource by its name.
     *
     * @return Map that contains attributes configuration (or empty map if no resources are configured)
     */
    @GET
    @Path("/{name}/operations")
    public Map getOperationsForResource(@PathParam("name") final String name) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        final Box<EntityMap<? extends OperationConfiguration>> container = BoxFactory.create(null);
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                final ManagedResourceConfiguration mrc =
                        currentConfig.getEntities(ManagedResourceConfiguration.class).get(name);
                if (mrc != null) {
                    container.set(mrc.getFeatures(OperationConfiguration.class));
                }
            });
        } finally {
            admin.release(bc);
        }
        return DTOFactory.buildOperations(container.get());
    }

    /**
     * Saves operations for certain configured resource by its name.
     *
     * @return no content
     */
    @PUT
    @Path("/{name}/operations")
    public Response setOperationsForResource(@PathParam("name") final String name,
                                         final Map<String, OperationDTOEntity> object) throws IOException {
        final BundleContext bc = Utils.getBundleContextOfObject(this);
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(bc, ConfigurationManager.class);
        assert admin != null;
        try {
            //verify first and second resources
            admin.get().processConfiguration(currentConfig -> {
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
                    return false;
                }
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }
}
