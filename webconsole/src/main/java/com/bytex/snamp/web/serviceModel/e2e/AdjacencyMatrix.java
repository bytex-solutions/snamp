package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.json.ThreadLocalJsonFactory;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.ComponentVertexIdentity;
import com.bytex.snamp.web.serviceModel.ObjectMapperSingleton;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializableWithType;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;

/**
 * Represents abstract class for building adjacency matrix.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AdjacencyMatrix implements Acceptor<ComponentVertex, ExceptionPlaceholder>, JsonSerializableWithType {
    private final Multimap<ComponentVertexIdentity, ComponentVertexIdentity> matrix = HashMultimap.create();

    abstract boolean filterRootComponent(final ComponentVertex vertex);

    abstract boolean filterChildComponent(final ComponentVertex vertex);

    void interceptVertex(final ComponentVertex vertex){

    }

    @Override
    public final void accept(final ComponentVertex vertex) {
        if (filterRootComponent(vertex)) {    //visit only root component and its linked nodes
            final ComponentVertexIdentity parentId = vertex.getIdentity();
            interceptVertex(vertex);
            for (final ComponentVertex child : vertex)
                if (filterChildComponent(child)) {
                    matrix.put(parentId, child.getIdentity());
                    interceptVertex(child);
                }
        }
    }

    @Override
    public final void serialize(final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        final ObjectNode serializedForm = ThreadLocalJsonFactory.getFactory().objectNode();
        //serialize matrix
        final ArrayNode vertices = ThreadLocalJsonFactory.getFactory().arrayNode();
        for (final ComponentVertexIdentity vertex : matrix.keySet()) {
            final ObjectNode vertexNode = ThreadLocalJsonFactory.getFactory().objectNode();
            vertexNode.put("vertex", ObjectMapperSingleton.INSTANCE.valueToTree(vertex));
            vertexNode.put("connections", ObjectMapperSingleton.INSTANCE.valueToTree(matrix.get(vertex)));
            vertices.add(vertexNode);
        }
        serializedForm.put("vertices", vertices);
        serializedForm.serialize(jsonGenerator, serializerProvider);
    }

    @Override
    public final void serializeWithType(final JsonGenerator jsonGenerator,
                                  final SerializerProvider serializerProvider,
                                  final TypeSerializer typeSerializer) throws IOException {
        serialize(jsonGenerator, serializerProvider);
    }
}
