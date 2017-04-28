package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.health.*;
import com.bytex.snamp.json.InstantSerializer;
import com.bytex.snamp.json.ThreadLocalJsonFactory;
import com.bytex.snamp.supervision.health.ClusterMalfunctionStatus;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.POJONode;

import java.io.IOException;

/**
 * Provides serialization of {@link HealthStatus} into JSON.
 */
public final class HealthStatusSerializer extends JsonSerializer<HealthStatus> {
    private static final String TYPE_FIELD = "@type";
    private static final String DATA_FIELD = "data";
    private static final String LEVEL_FIELD = "level";
    private static final String TIME_STAMP_FIELD = "timeStamp";
    private static final String DETAILS_FIELD = "details";

    private static void serialize(final OkStatus status, final ObjectNode output) {
        output.put(TYPE_FIELD, "OK");
    }

    private static String serialize(final MalfunctionStatus.Level level){
        return level.toString().toLowerCase();
    }

    private static void putCommonFields(final HealthStatus status, final ObjectNode output) {
        output.put(TYPE_FIELD, status.getClass().getSimpleName());
        output.put(DETAILS_FIELD, status.toString());
        output.put(TIME_STAMP_FIELD, InstantSerializer.serialize(status.getTimeStamp()));
    }

    private static void putCommonFields(final MalfunctionStatus status, final ObjectNode output) {
        putCommonFields((HealthStatus) status, output);
        final ObjectNode data = ThreadLocalJsonFactory.getFactory().objectNode();
        status.getData().forEach((key, value) -> data.put(key, new POJONode(value)));
        output.put(DATA_FIELD, data);
        output.put(LEVEL_FIELD, serialize(status.getLevel()));
    }

    private static void serialize(final InvalidAttributeValue status, final ObjectNode output) {
        putCommonFields(status, output);
        output.put("attributeName", status.getAttribute().getName());
        output.put("attributeValue", new POJONode(status.getAttribute().getValue()));
    }

    private static void serialize(final ResourceConnectorMalfunction status, final ObjectNode output) {
        putCommonFields(status,  output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    private static void serialize(final ConnectionProblem status, final ObjectNode output) {
        putCommonFields(status, output);
        output.put("error", status.getError().getLocalizedMessage());
    }

    @Override
    public void serialize(final HealthStatus status, final JsonGenerator jgen, final SerializerProvider provider) throws IOException {
        final ObjectNode node = ThreadLocalJsonFactory.getFactory().objectNode();
        if (status instanceof OkStatus)
            serialize((OkStatus) status, node);
        else if (status instanceof InvalidAttributeValue)
            serialize((InvalidAttributeValue) status, node);
        else if (status instanceof ResourceConnectorMalfunction)
            serialize((ResourceConnectorMalfunction) status, node);
        else if (status instanceof ConnectionProblem)
            serialize((ConnectionProblem) status, node);
        else if (status instanceof MalfunctionStatus)
            putCommonFields((MalfunctionStatus) status, node);
        else  //unknown status
            putCommonFields(status, node);
        node.serialize(jgen, provider);
    }
}
