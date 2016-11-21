package com.bytex.snamp.webconsole;

import com.bytex.snamp.Box;
import com.bytex.snamp.BoxFactory;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.webconsole.model.dto.AbstractDTOClass;
import com.bytex.snamp.webconsole.model.dto.DTOFactory;
import com.bytex.snamp.webconsole.model.dto.ManagedResourceConfigurationDTO;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
}
