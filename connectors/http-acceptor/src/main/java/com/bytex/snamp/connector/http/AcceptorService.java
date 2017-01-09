package com.bytex.snamp.connector.http;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.FixedKeysMap;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.dsp.notifications.NotificationSource;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.scripting.groovy.xml.XmlSlurperSlim;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.spi.resource.Singleton;
import groovy.json.JsonSlurper;
import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.StringReader;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

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

    private static abstract class HttpAcceptorHandler<I, E extends Throwable> extends ExposedServiceHandler<ManagedResourceConnector, I, E>{
        private HttpAcceptorHandler(){
            super(ManagedResourceConnector.class);
        }

        abstract boolean handleService(final HttpAcceptor acceptor, final I userData) throws E;

        @Override
        protected final boolean handleService(final ManagedResourceConnector service, final I userData) throws E {
            return !(service instanceof HttpAcceptor) || handleService((HttpAcceptor) service, userData);
        }
    }

    //represents loader of published HTTP acceptors in the form of cache with lazy values
    private static final class HttpAcceptorLoader extends CacheLoader<NotificationSource, HttpAcceptor>{
        @Override
        public HttpAcceptor load(@Nonnull final NotificationSource source) throws AcceptorNotFoundException {
            //used to find the appropriate acceptor
            final class HttpAcceptorFinder extends HttpAcceptorHandler<NotificationSource, ExceptionPlaceholder>{
                private HttpAcceptor acceptor;

                private HttpAcceptor getAcceptor(final NotificationSource source) throws AcceptorNotFoundException {
                    handleService(source);
                    if (acceptor == null)
                        throw new AcceptorNotFoundException();
                    else
                        return acceptor;
                }

                @Override
                boolean handleService(final HttpAcceptor acceptor, final NotificationSource source) {
                    final boolean found;
                    if(found = acceptor.represents(source))
                        this.acceptor = acceptor;
                    return !found;
                }
            }

            return new HttpAcceptorFinder().getAcceptor(source);
        }
    }

    private static final class Payload{
        private final Map<String, ?> headers;
        private final Object body;

        private Payload(final Map<String, ?> headers, final Object body){
            this.headers = Objects.requireNonNull(headers);
            this.body = Objects.requireNonNull(body);
        }
    }

    private static final class CustomPayloadDispatcher extends HttpAcceptorHandler<Payload, Exception>{
        @Override
        protected boolean handleService(final HttpAcceptor acceptor, final Payload userData) throws Exception {
            acceptor.dispatch(userData.headers, userData.body);
            return true;
        }
    }

    private final LoadingCache<NotificationSource, HttpAcceptor> acceptors;
    private final JsonSlurper jsonParser;
    private final CustomPayloadDispatcher payloadDispatcher;

    AcceptorService() {
        acceptors = CacheBuilder.newBuilder()
                .weakValues()
                .build(new HttpAcceptorLoader());
        jsonParser = new JsonSlurper();
        payloadDispatcher = new CustomPayloadDispatcher();
    }

    private static Map<String, ?> wrapHeaders(final HttpHeaders headers) {
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
    public Response acceptMeasurements(@Context final HttpHeaders headers, final Measurement[] measurements){
        for(final Measurement measurement: measurements)
            acceptMeasurement(headers, measurement);
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
    public Response acceptMeasurement(@Context final HttpHeaders headers, final Measurement measurement){
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
            LoggerProvider.getLoggerForObject(acceptor).log(Level.SEVERE, String.format("Failed to dispatch measurement %s", measurement), e);
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.noContent().build();
    }

    private void acceptCustomPayload(final Map<String, ?> headers, final Object body) throws Exception {
        payloadDispatcher.handleService(new Payload(headers, body));
    }

    @Consumes({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
    @POST
    @Produces({MediaType.TEXT_PLAIN})
    public Response acceptTextPayload(@Context final HttpHeaders headers, final String data){
        try {
            acceptCustomPayload(wrapHeaders(headers), data);
        } catch (final Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.noContent().build();
    }

    @Consumes({MediaType.APPLICATION_JSON})
    @POST
    @Produces({MediaType.TEXT_PLAIN})
    public Response acceptJsonPayload(@Context final HttpHeaders headers, final String json){
        try(final StringReader reader = new StringReader(json)) {
            acceptCustomPayload(wrapHeaders(headers), jsonParser.parse(reader));
        } catch (final Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.noContent().build();
    }

    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @POST
    @Produces({MediaType.TEXT_PLAIN})
    public Response acceptXmlPayload(@Context final HttpHeaders headers, final String xml) {
        try (final StringReader reader = new StringReader(xml)) {
            acceptCustomPayload(wrapHeaders(headers), new XmlSlurperSlim().parse(reader));
        } catch (final SAXException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        } catch (final Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        }
        return Response.noContent().build();
    }
}
