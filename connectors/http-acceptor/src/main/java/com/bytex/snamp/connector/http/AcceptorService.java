package com.bytex.snamp.connector.http;

import com.bytex.snamp.FixedKeysMap;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.md.notifications.NotificationSource;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.instrumentation.Measurement;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.bytex.snamp.internal.Utils.*;

/**
 * Represents REST service used to handle measurement and monitoring events through HTTP.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
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

    //represents loader of published HTTP acceptors in the form of cache with lazy values
    private static final class HttpAcceptorLoader extends CacheLoader<NotificationSource, HttpAcceptor>{
        @Override
        public HttpAcceptor load(final NotificationSource source) throws AcceptorNotFoundException {
            final BundleContext context = getBundleContextOfObject(this);
            final ServiceReference<?>[] services = context.getBundle().getRegisteredServices();
            for(final ServiceReference<?> ref: services)
                if(isInstanceOf(ref, ManagedResourceConnector.class)){
                    final ServiceHolder<?> connector = new ServiceHolder<>(context, ref);
                    try{
                        if(connector.get() instanceof HttpAcceptor){
                            final HttpAcceptor acceptor = (HttpAcceptor) connector.get();
                            if(acceptor.represents(source))
                                return acceptor;
                        }
                    } finally {
                        connector.release(context);
                    }
                }
            throw new AcceptorNotFoundException();
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

    /**
     * Consumes measurement from remote component.
     * @param measurement A measurement to accept.
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
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
        acceptor.dispatch(wrapHeaders(headers), measurement);
        return Response.noContent().build();
    }
}