package com.bytex.snamp.webconsole;

import com.bytex.snamp.security.web.Authenticator;

import javax.security.auth.login.*;
import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides API for SNAMP Web Console.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class WebConsoleService extends Authenticator {
    private static final String JAAS_REALM = "karaf";

    private final Logger logger;

    WebConsoleService(final Logger logger){
        this.logger = Objects.requireNonNull(logger);
    }

    @Path(WebConsoleServlet.AUTHENTICATE_PATH)
    @POST
    @Consumes("application/x-www-form-urlencoded")
    public Response authenticate(@FormParam("username") final String userName, @FormParam("password") final String password) throws WebApplicationException {
        final LoginContext context;
        try {
            context = new LoginContext(JAAS_REALM, new NamePasswordHandler(userName, password));
        } catch (final LoginException e) {
            logger.log(Level.SEVERE, "Cannot retrieve login context.", e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        logger.fine(() -> String.format("Trying to authenticate... Username is %s", userName));
        final String jwToken;
        //login and issue new JWT token
        try {
            jwToken = authenticate(context);
        } catch (final FailedLoginException | AccountException | CredentialException e) {
            logger.log(Level.WARNING, "Cannot login", e);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (final LoginException e) {
            logger.log(Level.SEVERE, "Login subsystem failed", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        logger.fine(() -> String.format("Successful authentication of user %s", userName));
        return Response
                .noContent()
                .cookie(new NewCookie(WebConsoleServlet.AUTH_COOKIE, jwToken, "/", "",
                        "SNAMP web console cookie", NewCookie.DEFAULT_MAX_AGE, false))
                .build();
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
}
