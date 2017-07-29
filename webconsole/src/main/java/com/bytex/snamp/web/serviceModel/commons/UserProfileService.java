package com.bytex.snamp.web.serviceModel.commons;

import com.bytex.snamp.web.serviceModel.RESTController;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.bytex.snamp.web.serviceModel.PrincipalBoundedService;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Exposes information about user profile.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
@Path("/")
public final class UserProfileService extends AbstractPrincipalBoundedService<UserProfile> implements RESTController {
    private static final String URL_CONTEXT = "/user";

    public UserProfileService(){
        super(UserProfile.class);
    }

    /**
     * Gets URL context of the service.
     *
     * @return URL context of the service.
     */
    @Override
    public String getUrlContext() {
        return URL_CONTEXT;
    }

    /**
     * Initializes this service.
     * <p/>
     * Services for SNAMP Web Console has lazy initialization. They will be initialized when the first session of the client
     * will be attached. This approach helps to save computation resources when SNAMP deployed as cluster with many nodes.
     */
    @Override
    protected void initialize() {

    }

    @Nonnull
    @Override
    protected UserProfile createUserData() {
        return new UserProfile();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public UserProfile getProfile(@Context final SecurityContext context){
        return getUserData(context);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/profile")
    public void setProfile(final UserProfile profile, @Context final SecurityContext context){
        setUserData(context, profile);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile/settings")
    public Map<String, ?> getSettings(@Context final SecurityContext security) {
        final BundleContext context = getBundleContext();
        final Map<String, Object> result = new HashMap<>();
        for (final ServiceReference<PrincipalBoundedService> serviceRef : WebConsoleService.createSelector().getServiceReferences(context, PrincipalBoundedService.class))
            WebConsoleService.getServiceName(serviceRef).ifPresent(serviceName -> {
                final PrincipalBoundedService<?> service = context.getService(serviceRef);
                if (service != null)
                    try {
                        result.put(serviceName, service.getUserData(security.getUserPrincipal()));
                    } finally {
                        context.ungetService(serviceRef);
                    }
            });
        return result;
    }

    @SuppressWarnings("unchecked")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/profile/settings")
    public void setSettings(final ObjectNode settings, @Context final SecurityContext security) {
        final BundleContext context = getBundleContext();
        final ObjectMapper reader = new ObjectMapper();
        settings.getFields().forEachRemaining(entry -> WebConsoleService.createSelector().property(WebConsoleService.SERVICE_NAME_PROPERTY, entry.getKey()).getServiceReference(context, PrincipalBoundedService.class).ifPresent(serviceRef -> {
            final PrincipalBoundedService service = context.getService(serviceRef);
            if (service != null)
                try {
                    final Object userData = reader.treeToValue(entry.getValue(), service.getUserDataType());
                    service.setUserData(security.getUserPrincipal(), userData);
                } catch (final IOException e) {
                    throw new WebApplicationException(e);
                } finally {
                    context.ungetService(serviceRef);
                }
        }));
    }
}
