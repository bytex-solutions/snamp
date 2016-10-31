package com.bytex.snamp.webconsole;

import org.osgi.service.cm.ConfigurationAdmin;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Provides API for SNAMP Web Console.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class WebConsoleService implements AutoCloseable {
    private static final String JAAS_REALM = "karaf";

    /**
     * The constant AUTH_COOKIE.
     */
    static final String AUTH_COOKIE = "snamp-auth-token";

    private final ConfigurationAdmin configAdmin;

    private static final Logger logger = Logger.getLogger(WebConsoleService.class.getName());

    /**
     * Instantiates a new Web console service.
     *
     * @param configAdmin the config admin
     */
    WebConsoleService(final ConfigurationAdmin configAdmin){
        this.configAdmin = Objects.requireNonNull(configAdmin);
    }

    @Path("/auth")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response authenticate(@FormParam("username") final String userName, @FormParam("password") final String password) throws WebApplicationException{
        final LoginContext context;
        logger.fine(String.format("Trying to authenticate... Username is %s", userName));
        try {
            context = new LoginContext(JAAS_REALM, new NamePasswordHandler(userName, password));
        } catch (final LoginException e){
            logger.severe("Cannot retrieve login context.");
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        //login and issue new JWT token
        try{
            context.login();
        } catch (final LoginException e){
            logger.warning(String.format("Cannot login - error occurred: %s ", e.getMessage()));
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        final Subject user = context.getSubject();
        if(user == null || user.getPrincipals().isEmpty()) {
            logger.warning("Cannot get any subject from login context");
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } else {
            logger.fine(String.format("Received subject is: %s", user));
            logger.fine(String.format("Received user principle is: %s", user.getPrincipals(org.apache.karaf.jaas.boot.principal.UserPrincipal.class)));
        }
        return Response
                .noContent()
                .cookie(new NewCookie(AUTH_COOKIE, issueAuthToken(user), "/", "",
                        "SNAMP web console cookie", NewCookie.DEFAULT_MAX_AGE, false))
                .build();
    }

    /**
     * Issue auth token string.
     *
     * @param user the user
     * @return the string
     */
    static String issueAuthToken(final Subject user){
        final JwtPrincipal principal = new JwtPrincipal(user);
        logger.fine(String.format("Forming JWT token for user %s with following params: %s", principal.getName(), principal));
        return principal.createJwtToken(JwtSecurityContext.SECRET);
    }

    /**
     * Dummy method with empty response - just to check if the index.html page was opened with necessary token.
     * @return 200 by default. If something goes wrong - auth filter will throw the exception
     */
    @Path("/check")
    @GET
    public Response checkAuth() {
        return Response.noContent().build();
    }

    @Override
    public void close() throws Exception {

    }
}
