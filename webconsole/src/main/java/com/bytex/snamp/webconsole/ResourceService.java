package com.bytex.snamp.webconsole;

import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.configuration.*;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.webconsole.model.dto.AbstractDTOClass;
import com.bytex.snamp.webconsole.model.dto.DTOFactory;
import com.bytex.snamp.webconsole.model.dto.ManagedResourceConfigurationDTO;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Attr;

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
public final class ResourceService {
    /**
     * Sample method for retrieving configuration of managed resources.
     * @return Map that contains configuration (or empty map if no resources are configured)
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configuration")
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
     * Sample method for retrieving configuration of managed resources.
     * @return Map that contains configuration (or empty map if no resources are configured)
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configuration/{name}")
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
     * @return Map that contains configuration (or empty map if no resources are configured)
     * @throws IOException
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/configuration/{name}")
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

                return false;
            });
        } finally {
            admin.release(bc);
        }
        return Response.noContent().build();
    }
}
