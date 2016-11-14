package com.bytex.snamp.connector.http;

import com.bytex.snamp.FixedKeysMap;
import com.bytex.snamp.instrumentation.Measurement;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import java.util.List;

/**
 * Represents REST service used to handle measurement and monitoring events through HTTP.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class AcceptorService {

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
        final FixedKeysMap<String, List<String>> hdrs = wrapHeaders(headers);
        return Response.noContent().build();
    }
}
