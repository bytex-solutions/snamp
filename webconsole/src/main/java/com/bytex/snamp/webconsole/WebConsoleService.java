package com.bytex.snamp.webconsole;

import com.bytex.snamp.security.web.Authenticator;

import javax.security.auth.login.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides API for SNAMP Web Console.
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class WebConsoleService extends Authenticator {
    private static final String JAAS_REALM = "karaf";

    private final Logger logger;

    /**
     * Instantiates a new Web console service.
     *
     * @param logger the logger
     */
    WebConsoleService(final Logger logger){
        this.logger = Objects.requireNonNull(logger);
    }

    /**
     * Authenticate response.
     *
     * @param userName the user name
     * @param password the password
     * @return the response
     * @throws WebApplicationException the web application exception
     */
    @Path(WebConsoleServlet.AUTHENTICATE_PATH)
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
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
     * Gets active user name.
     *
     * @return the active user name
     */
    @Path("/username")
    @GET
    public String getActiveUserName(@Context final SecurityContext sc) {
        return sc.getUserPrincipal().getName();
    }
}
