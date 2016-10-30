package com.bytex.snamp.webconsole;

import com.auth0.jwt.JWTSigner;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.*;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides API for SNAMP Web Console.
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class WebConsoleService implements AutoCloseable {
    private static final String JAAS_REALM = "karaf";
    private static final String AUTH_COOKIE = "snamp-auth-token";
    private final ConfigurationAdmin configAdmin;

    static final String secret = "{{secret used for signing}}";

    private static final Logger logger = Logger.getLogger(WebConsoleService.class.getName());

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

    private static String issueAuthToken(final Subject user){
        final JWTSigner signer = new JWTSigner(secret);
        final HashMap<String, Object> claims = new HashMap<>();
        final long iat = System.currentTimeMillis() / 1000L; // issued at claim
        final long exp = iat + 604800L; // expires claim. In this case the token expires in 1 week

        final String roles = user.getPrincipals(RolePrincipal.class)
                .stream()
                .map(RolePrincipal::getName)
                .collect(Collectors.joining(";"));

        final Set<UserPrincipal> userPrincipal = user.getPrincipals(UserPrincipal.class);
        if (userPrincipal.isEmpty()) {
            logger.warning("For some reasons Subject does not contain user principal");
        }
        final String userName = userPrincipal
                .stream()
                .map(UserPrincipal::getName)
                .collect(Collectors.joining(";"));

        claims.put("sub", userName);
        claims.put("roles", roles);
        claims.put("exp", exp);
        claims.put("iat", iat);
        logger.fine(String.format("Forming JWT token for user %s with following params: %s", userName, claims));
        return signer.sign(claims);
    }

    @GET
    public Response seyHello() {
        return Response.ok().entity( "Yes, it works." ).build();
    }

    @Override
    public void close() throws Exception {

    }
}
