package com.bytex.snamp.webconsole;

import com.bytex.snamp.Box;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EntityMap;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.internal.Utils;
import com.google.gson.Gson;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Provides API for SNAMP configuration management.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/management")
@Singleton
public final class ManagementService {
    private final ConfigurationAdmin configAdmin;
    private final Gson formatter;

    ManagementService(final ConfigurationAdmin configAdmin){
        this.configAdmin = Objects.requireNonNull(configAdmin);
        this.formatter = new Gson();
    }

    /**
     * Sample method for retrieving configuration of managed resources.
     * @return Map that contains configuration (or empty map if no resources are configured)
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/configuration")
    public Map getConfiguration() throws IOException {
        final ServiceHolder<ConfigurationManager> admin = ServiceHolder.tryCreate(Utils.getBundleContextOfObject(this),
                ConfigurationManager.class);
        assert admin != null;
        final Box<Object> box = DistributedServices.getProcessLocalBox("box");
        try {
            //verify first and second resources
            admin.get().readConfiguration(currentConfig -> {
                box.set(currentConfig.getEntities(ManagedResourceConfiguration.class));
            });
        } finally {
            admin.release(Utils.getBundleContextOfObject(this));
        }
        if (box.hasValue()) {
            return (EntityMap) box.get();
        } else {
            return Collections.EMPTY_MAP;
        }
    }
}
