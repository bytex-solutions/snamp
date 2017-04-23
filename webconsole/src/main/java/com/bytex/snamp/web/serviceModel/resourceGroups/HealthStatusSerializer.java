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

    private static void putCommonFields(final MalfunctionStatus status, final String type, final ObjectNode output) {
        output.put(TYPE_FIELD, type);
        output.put(IS_CRITICAL_FIELD, status.isCritical());
        final ObjectNode data = ThreadLocalJsonFactory.getFactory().objectNode();
        status.getData().forEach((key, value) -> data.put(key, new POJONode(value)));
        output.put("data", data);
    }

    private static void putClusterMalfunctionFields(final ClusterMalfunctionStatus status, final String type, final ObjectNode output) {
        putCommonFields(status, type, output);
        output.put(CLUSTER_NAME_FIELD, status.getClusterName());
    }

    private static void serialize(final ClusterRecoveryStatus status, final ObjectNode output) {
        putClusterMalfunctionFields(status, "InvalidAttributeValue", output);
    }

    private static void putResourceMalfunctionFields(final ResourceMalfunctionStatus status, final String type, final ObjectNode output) {
        putCommonFields(status, type, output);
        output.put(RESOURCE_NAME_FIELD, status.getResourceName());
    }

    private static void serialize(final InvalidAttributeValue status, final ObjectNode output) {
        putResourceMalfunctionFields(status, "InvalidAttributeValue", output);
        output.put("attributeName", status.getAttribute().getName());
        output.put("attributeValue", new POJONode(status.getAttribute().getValue()));
    }

    private static void serialize(final ResourceIsNotAvailable status, final ObjectNode output) {
        putResourceMalfunctionFields(status, "ResourceIsNotAvailable", output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    private static void serialize(final ConnectionProblem status, final ObjectNode output) {
        putResourceMalfunctionFields(status, "ConnectionProblem", output);
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
        else if (status instanceof ClusterRecoveryStatus)
            serialize((ClusterRecoveryStatus) status, node);
        else if (status instanceof ClusterMalfunctionStatus) {
            putClusterMalfunctionFields((ClusterMalfunctionStatus) status, "ClusterMalfunction", node);
            node.put("details", status.toString());
        }
        else if(status instanceof ResourceMalfunctionStatus){
            putResourceMalfunctionFields((ResourceMalfunctionStatus) status, "ResourceMalfunction", node);
            node.put("details", status.toString());
        } else { //unknown status
            node.put(TYPE_FIELD, "Unknown");
            node.put(IS_CRITICAL_FIELD, status.isCritical());
            node.put("details", status.toString());
        }
        node.serialize(jgen, provider);
    }
}
