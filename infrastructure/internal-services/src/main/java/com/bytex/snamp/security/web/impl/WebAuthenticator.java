package com.bytex.snamp.security.web.impl;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.security.web.JWTAuthFilter;
import com.bytex.snamp.security.web.JWTAuthenticator;

import javax.security.auth.login.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path('/' + WebAuthenticator.PATH)
public final class WebAuthenticator extends JWTAuthenticator {
    private static final Pattern HOST_NAME_PATTERN = Pattern.compile("(?<hn>([\\p{IsAlphabetic}\\d.\\-;@]+)|(\\[.+]))(:[0-9]+)?");

    /**
     * Represents URL path to this service.
     */
    static final String PATH = "login";

    /**
     * Default JAAS realm used for instantiating login context.
     */
    private static final String JAAS_REALM = "karaf";

    /**
     * Default name of cookie with authentication token.
     */
    private static final String AUTH_COOKIE = JWTAuthFilter.DEFAULT_AUTH_COOKIE;


    WebAuthenticator(final ClusterMember clusterMember){
        super(clusterMember);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response authenticate(@FormParam("username") final String userName,
                                 @FormParam("password") final String password,
                                 @HeaderParam("Host") final String hostName,
                                 @Context final SecurityContext security) throws WebApplicationException {
        final LoginContext context;
        try {
            context = new LoginContext(JAAS_REALM, new NamePasswordHandler(userName, password));
        } catch (final LoginException e) {
            getLogger().log(Level.SEVERE, "Cannot retrieve login context.", e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        getLogger().fine(() -> String.format("Trying to authenticate... Username is %s", userName));
        final String jwToken;
        //login and issue new JWT token
        try {
            jwToken = authenticate(context);
        } catch (final FailedLoginException | AccountException | CredentialException e) {
            getLogger().log(Level.WARNING, "Cannot login", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (final LoginException e) {
            getLogger().log(Level.SEVERE, "Login subsystem failed", e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        getLogger().fine(() -> String.format("Successful authentication of user %s", userName));
        final int COOKIE_AGE = 30 * 24 * 60 * 60;   //30 days
        final Matcher m = HOST_NAME_PATTERN.matcher(hostName);
        if(m.matches())
            return Response
                    .noContent()
                    .cookie(new NewCookie(AUTH_COOKIE, jwToken, "/", m.group(1),
                            "SNAMP JWT authentication token", COOKIE_AGE, security.isSecure()))
                    .build();
        else
            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Invalid host name " + hostName).build());
    }

    /**
     * Gets active user name.
     *
     * @return the active user name
     */
    @Path("/username")
    @GET
    public String getCurrentUser(@Context final SecurityContext sc) {
        return sc.getUserPrincipal().getName();
    }
}
