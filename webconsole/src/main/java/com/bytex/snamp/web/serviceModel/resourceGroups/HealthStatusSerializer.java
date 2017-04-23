package com.bytex.snamp.web.serviceModel.resourceGroups;

import com.bytex.snamp.connector.health.*;
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
    private static final String CLUSTER_NAME_FIELD = "clusterName";
    private static final String IS_CRITICAL_FIELD = "critical";

    private static void serialize(final OkStatus status, final ObjectNode output) {
        output.put(TYPE_FIELD, "OK");
    }

    private static void putCommonFields(final HealthStatus status, final ObjectNode output){
        output.put(TYPE_FIELD, status.getClass().getSimpleName());
        output.put(IS_CRITICAL_FIELD, status.isCritical());
        output.put("details", status.toString());
    }

    private static void putCommonFields(final MalfunctionStatus status, final ObjectNode output) {
        putCommonFields((HealthStatus) status, output);
        final ObjectNode data = ThreadLocalJsonFactory.getFactory().objectNode();
        status.getData().forEach((key, value) -> data.put(key, new POJONode(value)));
        output.put("data", data);
    }

    private static void putClusterMalfunctionFields(final ClusterMalfunctionStatus status, final ObjectNode output) {
        putCommonFields(status, output);
        output.put(CLUSTER_NAME_FIELD, status.getClusterName());
    }

    private static void putResourceMalfunctionFields(final ResourceMalfunctionStatus status, final ObjectNode output) {
        putCommonFields(status, output);
        output.put(RESOURCE_NAME_FIELD, status.getResourceName());
    }

    private static void serialize(final InvalidAttributeValue status, final ObjectNode output) {
        putResourceMalfunctionFields(status, output);
        output.put("attributeName", status.getAttribute().getName());
        output.put("attributeValue", new POJONode(status.getAttribute().getValue()));
    }

    private static void serialize(final ResourceIsNotAvailable status, final ObjectNode output) {
        putResourceMalfunctionFields(status,  output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    private static void serialize(final ConnectionProblem status, final ObjectNode output) {
        putResourceMalfunctionFields(status, output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    @Override
    public void serialize(final HealthStatus status, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        final ObjectNode node = ThreadLocalJsonFactory.getFactory().objectNode();
        if (status instanceof OkStatus)
            serialize((OkStatus) status, node);
        else if (status instanceof InvalidAttributeValue)
            serialize((InvalidAttributeValue) status, node);
        else if (status instanceof ResourceIsNotAvailable)
            serialize((ResourceIsNotAvailable) status, node);
        else if (status instanceof ConnectionProblem)
            serialize((ConnectionProblem) status, node);
        else if (status instanceof ClusterMalfunctionStatus) {
            putClusterMalfunctionFields((ClusterMalfunctionStatus) status, node);
        } else if (status instanceof ResourceMalfunctionStatus) {
            putResourceMalfunctionFields((ResourceMalfunctionStatus) status, node);
        } else { //unknown status
            putCommonFields(status, node);
        }
        node.serialize(jgen, provider);
    }
}
