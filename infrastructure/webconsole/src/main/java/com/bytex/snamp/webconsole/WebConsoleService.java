package com.bytex.snamp.webconsole;

import org.osgi.service.cm.ConfigurationAdmin;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 * Provides API for SNAMP Web Console.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/webconsole")
public final class WebConsoleService implements AutoCloseable {
    private static final String JAAS_REALM = "karaf";
    private static final String AUTH_COOKIE = "snamp-auth-token";
    private final ConfigurationAdmin configAdmin;

    WebConsoleService(final ConfigurationAdmin configAdmin){
        this.configAdmin = Objects.requireNonNull(configAdmin);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response authenticate(@FormParam("username") final String userName, @FormParam("password") final String password) throws WebApplicationException{
        final LoginContext context;
        try {
            context = new LoginContext(JAAS_REALM, new NamePasswordHandler(userName, password));
        } catch (final LoginException e){
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        //login and issue new JWT token
        try{
            context.login();
        } catch (final LoginException e){
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        final Subject user = context.getSubject();
        if(user == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        return Response
                .noContent()
                .cookie(new NewCookie(AUTH_COOKIE, issueAuthToken(user)))
                .build();
    }

    private static String issueAuthToken(final Subject user){
        return "";
    }

    @GET
    public Response seyHello() {
        return Response.ok().entity( "Yes, it works." ).build();
    }

    @Override
    public void close() throws Exception {

    }
}
