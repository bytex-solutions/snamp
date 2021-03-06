package com.bytex.snamp.connector.http;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.dataStream.DataStreamConnector;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.scripting.groovy.xml.XmlSlurperSlim;
import com.sun.jersey.spi.resource.Singleton;
import groovy.json.JsonSlurper;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.xml.sax.SAXException;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.StringReader;
import java.util.*;

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

    private static Response noContent(){
        return Response.noContent().build();
    }

    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        final Version version = getBundleContextOfObject(this).getBundle().getVersion();
        final String httpAcceptorType = ManagedResourceConnector.getConnectorType(HttpAcceptor.class);
        final Set<String> sources = new HashSet<>();
        final BundleContext context = getBundleContext();
        for (final String resourceName : ManagedResourceConnectorClient.selector().getResources(context))
            ManagedResourceConnectorClient.tryCreate(context, resourceName).ifPresent(client -> {
                final String connectorType = client.getConnectorType();
                if (Objects.equals(httpAcceptorType, connectorType))
                    sources.add(resourceName);
                client.close();
            });
        final String responseBody = String.format("HTTP Acceptor, version=%s, sources=%s", version, sources);
        return Response.ok()
                .entity(responseBody)
                .build();
    }

    @Path("/measurements")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response acceptMeasurements(@Context final HttpHeaders headers, final Measurement[] measurements) {
        return Arrays.stream(measurements)
                .map(measurement -> acceptMeasurement(headers, measurement))
                .reduce(noContent(), (result, response) -> response.getStatus() > result.getStatus() ? response : result);
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private static Response acceptMeasurement(final ManagedResourceConnectorClient client,
                                              final HttpHeaders headers,
                                              final Measurement measurement) {
        try {
            return client.queryObject(DataStreamConnector.class)
                    .map(acceptor -> {
                        try {
                            acceptor.dispatch(wrapHeaders(headers), measurement);
                        } catch (final Exception e) {
                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.toString()).build();
                        }
                        return noContent();
                    })
                    .orElseGet(() -> Response.status(Response.Status.BAD_REQUEST).entity("Resource %s is not data stream processor").build());
        } finally {
            client.close();
        }
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
        final BundleContext context = getBundleContext();
        return ManagedResourceConnectorClient.tryCreate(context, measurement.getInstanceName())
                .map(client -> acceptMeasurement(client, headers, measurement))
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND).build());
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
