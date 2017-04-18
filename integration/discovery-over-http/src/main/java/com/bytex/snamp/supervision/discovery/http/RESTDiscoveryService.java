package com.bytex.snamp.supervision.discovery.http;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.supervision.SupervisorClient;
import com.bytex.snamp.supervision.discovery.InvalidResourceGroupException;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryException;
import com.bytex.snamp.supervision.discovery.ResourceDiscoveryService;
import com.bytex.snamp.supervision.discovery.ResourceGroupNotFoundException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Exposes Resource Discovery through HTTP.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/{groupName}")
public final class RESTDiscoveryService {
    private static final int NOT_IMPLEMENTED_STATUS = 501;

    /**
     * Represents DTO for resource announcement.
     */
    public static final class ResourceAnnouncement{
        private String connectionString;
        private final Map<String, String> parameters;

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public ResourceAnnouncement(){
            connectionString = "";
            parameters = new HashMap<>();
        }

        @JsonProperty("connectionString")
        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public String getConnectionString(){
            return connectionString;
        }

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public void setConnectionString(@Nonnull final String value){
            connectionString = value;
        }

        @JsonProperty("parameters")
        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public Map<String, String> getParameters(){
            return parameters;
        }

        @SpecialUse(SpecialUse.Case.SERIALIZATION)
        public void setParameters(@Nonnull final Map<String, String> value){
            parameters.clear();
            parameters.putAll(value);
        }
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private static WebApplicationException unsupervisedGroup(final String groupName){
        return new WebApplicationException(Response
                .status(NOT_IMPLEMENTED_STATUS)
                .entity(String.format("Supervisor is not assigned for group %s", groupName))
                .build());
    }

    private static WebApplicationException unsupportedDiscovery(final String groupName){
        return new WebApplicationException(Response
                .status(NOT_IMPLEMENTED_STATUS)
                .entity(String.format("Supervisor for group %s doesn't support discovery service", groupName))
                .build());
    }

    private <E extends Throwable> void withDiscoveryService(final String groupName,
                                                            final Acceptor<? super ResourceDiscoveryService, E> acceptor) throws E {
        final Optional<SupervisorClient> clientRef = SupervisorClient.tryCreate(getBundleContext(), groupName);
        if (clientRef.isPresent())
            try (final SupervisorClient client = clientRef.get()) {
                final ResourceDiscoveryService discoveryService = client.queryObject(ResourceDiscoveryService.class)
                        .orElseThrow(() -> unsupportedDiscovery(groupName));
                acceptor.accept(discoveryService);
            }
        else
            throw unsupervisedGroup(groupName);
    }

    private static void registerResource(final ResourceDiscoveryService discoveryService,
                                         final String resourceName,
                                         final ResourceAnnouncement announcement) throws ResourceDiscoveryException {
        try {
            discoveryService.registerResource(resourceName, announcement.getConnectionString(), announcement.getParameters());
        } catch (final InvalidResourceGroupException e){
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (final ResourceGroupNotFoundException e){
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Path("/{resourceName}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerResource(@PathParam("groupName") final String groupName,
                                 @PathParam("resourceName") final String resourceName,
                                 final ResourceAnnouncement announcement) throws ResourceDiscoveryException {
        withDiscoveryService(groupName, discoveryService -> registerResource(discoveryService, resourceName, announcement));
    }

    private static void removeResource(final ResourceDiscoveryService discoveryService,
                                       final String resourceName) throws ResourceDiscoveryException {
        try {
            if(!discoveryService.removeResource(resourceName))
                throw new WebApplicationException(Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(String.format("Resource %s doesn't exist", resourceName))
                        .build());
        } catch (final InvalidResourceGroupException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (final ResourceGroupNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }

    @DELETE
    @Path("/{resourceName}")
    public void removeResource(@PathParam("groupName") final String groupName,
                               @PathParam("resourceName") final String resourceName) throws ResourceDiscoveryException {
        withDiscoveryService(groupName, discoveryService -> removeResource(discoveryService, resourceName));
    }

    @DELETE
    @Path("/")
    public void removeAllResources(@PathParam("groupName") final String groupName) throws ResourceDiscoveryException {
        withDiscoveryService(groupName, ResourceDiscoveryService::removeAllResources);
    }
}
