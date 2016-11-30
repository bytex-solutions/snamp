package com.bytex.snamp.connector.http;

import com.bytex.snamp.FixedKeysMap;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.instrumentation.Measurement;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.spi.resource.Singleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Level;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;
import static com.bytex.snamp.internal.Utils.isInstanceOf;

/**
 * Represents REST service used to handle measurement and monitoring events through HTTP.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Singleton
@Path("/")
public final class AcceptorService {
    private static final class AcceptorNotFoundException extends Exception{
        private static final long serialVersionUID = 7841487883004602958L;

        private AcceptorNotFoundException(){
        }

        @Override
        public AcceptorNotFoundException fillInStackTrace() {
            return this;
        }
    }

    private static final class HttpAcceptorFinder extends ExposedServiceHandler<ManagedResourceConnector, Void>{
        private final BundleContext context;
        private HttpAcceptor acceptor;

        private HttpAcceptorFinder(final BundleContext context, final NotificationSource source) {
            super(ManagedResourceConnector.class, ref -> filter(ref, context, source));
            this.context = context;
        }

        private static boolean filter(final ServiceReference<?> ref, final BundleContext context, final NotificationSource source){
            final ServiceHolder<?> connector = new ServiceHolder<>(context, ref);
            try{
                return connector.get() instanceof HttpAcceptor && ((HttpAcceptor) connector.get()).represents(source);
            } finally {
                connector.release(context);
            }
        }

        @Override
        protected BundleContext getBundleContext() {
            return context;
        }

        @Override
        protected void handleService(final ManagedResourceConnector service, final Void userData) {
            acceptor = (HttpAcceptor) service;
        }

        private HttpAcceptor getAcceptor() throws AcceptorNotFoundException {
            if (acceptor == null)
                throw new AcceptorNotFoundException();
            else
                return acceptor;
        }
    }

    //represents loader of published HTTP acceptors in the form of cache with lazy values
    private static final class HttpAcceptorLoader extends CacheLoader<NotificationSource, HttpAcceptor>{
        @Override
        public HttpAcceptor load(final NotificationSource source) throws AcceptorNotFoundException {
            final HttpAcceptorFinder finder = new HttpAcceptorFinder(getBundleContextOfObject(this), source);
            return finder.getAcceptor();
        }
    }

    private final LoadingCache<NotificationSource, HttpAcceptor> acceptors;

    AcceptorService(){
        acceptors = CacheBuilder.newBuilder()
                .weakValues()
                .build(new HttpAcceptorLoader());
    }

    private static FixedKeysMap<String, List<String>> wrapHeaders(final HttpHeaders headers) {
        final MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        return FixedKeysMap.readOnlyMap(requestHeaders::get, requestHeaders.keySet());
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping(){
        final Version version = getBundleContextOfObject(this).getBundle().getVersion();
        final String sources = Joiner.on(';').join(acceptors.asMap().keySet());
        final String responseBody = String.format("HTTP Acceptor, version=%s, sources=%s", version, sources);
        return Response.ok()
                .entity(responseBody)
                .build();
    }

    @Path("/measurements")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response accept(final Measurement[] measurements, @Context final HttpHeaders headers){
        for(final Measurement measurement: measurements)
            accept(measurement, headers);
        return Response.noContent().build();
    }

    /**
     * Consumes measurement from remote component.
     * @param measurement A measurement to accept.
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/measurement")
    public Response accept(final Measurement measurement, @Context final HttpHeaders headers){
        final NotificationSource source = new NotificationSource(measurement.getComponentName(), measurement.getInstanceName());
        //find the appropriate connector and redirect
        final HttpAcceptor acceptor;
        try{
            acceptor = acceptors.get(source);
        } catch (final ExecutionException e){
            if(e.getCause() instanceof AcceptorNotFoundException)
                throw new WebApplicationException(e.getCause(), Response
                        .status(Response.Status.NOT_FOUND)
                        .type(MediaType.TEXT_PLAIN_TYPE)
                        .entity(String.format("Acceptor for %s doesn't exist", source))
                        .build());
            else
                throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        //acceptor is found. Redirect measurement to it
        try {
            acceptor.dispatch(wrapHeaders(headers), measurement);
        } catch (final Exception e) {
            acceptor.getLogger().log(Level.SEVERE, String.format("Failed to dispatch measurement %s", measurement), e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.noContent().build();
    }

    @Consumes
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response accept(final String data, @Context final HttpHeaders headers){
        return Response.noContent().build();
    }
}
