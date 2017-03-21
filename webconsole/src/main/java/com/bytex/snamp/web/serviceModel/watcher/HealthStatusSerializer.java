package com.bytex.snamp.web.serviceModel.watcher;

import com.bytex.snamp.connector.supervision.*;
import com.bytex.snamp.json.ThreadLocalJsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.POJONode;

import java.io.IOException;

/**
 * Provides serialization of {@link HealthStatus} into JSON.
 */
final class HealthStatusSerializer extends JsonSerializer<HealthStatus> {
    private static final String TYPE_FIELD = "@type";
    private static final String RESOURCE_NAME_FIELD = "resourceName";
    private static final String IS_CRITICAL_FIELD = "critical";

    private static void serialize(final OkStatus status, final ObjectNode output) {
        output.put(TYPE_FIELD, "OK");
    }

    private static void putCommonFields(final MalfunctionStatus status, final String type, final ObjectNode output){
        output.put(TYPE_FIELD, type);
        output.put(RESOURCE_NAME_FIELD, status.getResourceName());
        output.put(IS_CRITICAL_FIELD, status.isCritical());

    }

    private static void serialize(final InvalidAttributeValue status, final ObjectNode output) {
        putCommonFields(status, "InvalidAttributeValue", output);
        output.put("attributeName", status.getAttribute().getName());
        output.put("attributeValue", new POJONode(status.getAttribute().getValue()));
    }

    private static void serialize(final ResourceInGroupIsNotUnavailable status, final ObjectNode output) {
        putCommonFields(status, "ResourceInGroupIsNotUnavailable", output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    private static void serialize(final ConnectionProblem status, final ObjectNode output) {
        putCommonFields(status, "ConnectionProblem", output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    @Override
    public void serialize(final HealthStatus status, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        final ObjectNode node = ThreadLocalJsonFactory.getFactory().objectNode();
        if (status instanceof OkStatus)
            serialize((OkStatus) status, node);
        else if (status instanceof InvalidAttributeValue)
            serialize((InvalidAttributeValue) status, node);
        else if (status instanceof ResourceInGroupIsNotUnavailable)
            serialize((ResourceInGroupIsNotUnavailable) status, node);
        else if (status instanceof ConnectionProblem)
            serialize((ConnectionProblem) status, node);
        else if (status instanceof MalfunctionStatus) { //unknown status
            putCommonFields((MalfunctionStatus) status, "UnknownMalfunction", node);
            node.put("details", status.toString());
        }
        node.serialize(jgen, provider);
    }
}