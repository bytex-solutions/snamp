package com.bytex.snamp.connector.zipkin;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.core.ExposedServiceHandler;
import zipkin.Codec;
import zipkin.Span;
import zipkin.storage.AsyncSpanConsumer;
import zipkin.storage.Callback;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/v1")
public final class ZipkinHttpService {
    private static final class SpanDispatcher extends ExposedServiceHandler<ManagedResourceConnector, List<Span>>{
        private SpanDispatcher() {
            super(ManagedResourceConnector.class);
        }

        @Override
        protected void handleService(final ManagedResourceConnector service, final List<Span> spans) {
            if(service instanceof AsyncSpanConsumer)
                ((AsyncSpanConsumer) service).accept(spans, Callback.NOOP);
        }
    }

    private final SpanDispatcher dispatcher;

    ZipkinHttpService(){
        dispatcher = new SpanDispatcher();
    }


    private Response receiveSpans(final List<Span> spans){
        dispatcher.handleService(spans);
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @POST
    @Path("/spans")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response receiveTextSpans(final byte[] spans){
        return receiveSpans(Codec.JSON.readSpans(spans));
    }

    @POST
    @Path("/spans")
    @Consumes("application/x-thrift")
    public Response receiveBinarySpans(final byte[] spans){
        return receiveSpans(Codec.THRIFT.readSpans(spans));
    }
}
