package com.bytex.snamp.connector.http;

import com.bytex.snamp.Aggregator;
import com.bytex.snamp.MapUtils;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.dsp.DataStreamConnector;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.scripting.groovy.xml.XmlSlurperSlim;
import com.sun.jersey.spi.resource.Singleton;
import groovy.json.JsonSlurper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import javax.management.InstanceNotFoundException;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private static final class Payload{
        private final Map<String, ?> headers;
        private final Object body;

        private Payload(final Map<String, ?> headers, final Object body){
            this.headers = Objects.requireNonNull(headers);
            this.body = Objects.requireNonNull(body);
        }
    }

    private static final class CustomPayloadDispatcher extends ExposedServiceHandler<ManagedResourceConnector, Payload, Exception> {
        private CustomPayloadDispatcher() {
            super(ManagedResourceConnector.class);
        }

        @Override
        protected boolean handleService(final ManagedResourceConnector service, final Payload userData) throws Exception {
            if (service instanceof DataStreamConnector)
                ((DataStreamConnector) service).dispatch(userData.headers, userData.body);
            return true;
        }
    }

    private final JsonSlurper jsonParser;
    private final CustomPayloadDispatcher payloadDispatcher;

    AcceptorService() {
        jsonParser = new JsonSlurper();
        payloadDispatcher = new CustomPayloadDispatcher();
    }

    private static Map<String, ?> wrapHeaders(final HttpHeaders headers) {
        final MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
        return MapUtils.readOnlyMap(requestHeaders::get, requestHeaders.keySet());
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        final Version version = getBundleContextOfObject(this).getBundle().getVersion();
        final String httpAcceptorType = ManagedResourceConnector.getConnectorType(HttpAcceptor.class);
        final Set<String> sources = new HashSet<>();
        for (final ServiceReference<ManagedResourceConnector> connectorRef : ManagedResourceConnectorClient.getConnectors(getBundleContext()).values()) {
            final String connectorType = ManagedResourceConnector.getConnectorType(connectorRef.getBundle());
            if (Objects.equals(httpAcceptorType, connectorType))
                sources.add(ManagedResourceConnectorClient.getManagedResourceName(connectorRef));
        }
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

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    /**
     * Consumes measurement from remote component.
     * @param measurement A measurement to accept.
     */
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/measurement")
    public Response acceptMeasurement(@Context final HttpHeaders headers, final Measurement measurement) {
        //find the appropriate connector and redirect
        ManagedResourceConnectorClient client = null;
        final Response response;
        try {
            client = new ManagedResourceConnectorClient(getBundleContext(), measurement.getInstanceName());
            if (Aggregator.queryAndAccept(client, DataStreamConnector.class, acceptor -> acceptor.dispatch(wrapHeaders(headers), measurement)))
                response = Response.noContent().build();
            else
                response = Response.status(Response.Status.BAD_REQUEST).entity("Resource %s is not data stream processor").build();
        } catch (final InstanceNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } catch (final Exception e) {
            throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            if (client != null)
                client.release(getBundleContext());
        }
        return response;
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
