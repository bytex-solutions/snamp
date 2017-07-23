package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.json.ThreadLocalJsonFactory;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.ComponentVertexIdentity;
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
 * Abstract adjacency matrix.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AdjacencyMatrix implements Acceptor<ComponentVertex, ExceptionPlaceholder>, JsonSerializableWithType {
    private final Multimap<ComponentVertexIdentity, ComponentVertexIdentity> matrix = HashMultimap.create();

    final void setAdjacency(final ComponentVertex source, final ComponentVertex destination){
        matrix.put(source.getIdentity(), destination.getIdentity());
    }

    final boolean hasAdjacency(final ComponentVertex source, final ComponentVertex destination){
        return matrix.containsEntry(source.getIdentity(), destination.getIdentity());
    }


    @Override
    public abstract void accept(final ComponentVertex vertex);

    abstract void serialize(final ObjectNode node);

    @Override
    public final void serialize(final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
        final ObjectNode serializedForm = ThreadLocalJsonFactory.getFactory().objectNode();
        //serialize matrix
        final ObjectNode vertices = ThreadLocalJsonFactory.getFactory().objectNode();
        for(final ComponentVertexIdentity source: matrix.keySet()){
            final ArrayNode sourceNode = ThreadLocalJsonFactory.getFactory().arrayNode();
            for(final ComponentVertexIdentity destination: matrix.get(source))
                sourceNode.add(destination.toString());
            vertices.put(source.toString(), sourceNode);
        }
        serializedForm.put("vertices", vertices);
        serialize(serializedForm);
        serializedForm.serialize(jsonGenerator, serializerProvider);
    }

    @Override
    public final void serializeWithType(final JsonGenerator jsonGenerator,
                                  final SerializerProvider serializerProvider,
                                  final TypeSerializer typeSerializer) throws IOException {
        serialize(jsonGenerator, serializerProvider);
    }
}
