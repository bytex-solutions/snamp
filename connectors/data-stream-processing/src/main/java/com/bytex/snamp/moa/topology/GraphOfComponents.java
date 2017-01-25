package com.bytex.snamp.moa.topology;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.google.common.base.MoreObjects;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Provides analysis of topology between components based on stream of {@link Span}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class GraphOfComponents extends ConcurrentHashMap<String, ComponentVertex> implements Consumer<Span>, Stateful {//key in map is a component name
    private static final long serialVersionUID = 2292647118511712487L;
    private final ConcurrentLinkedHashMap<Identifier, ComponentVertex> idToVertexCache; //key is a spanID of the node

    public GraphOfComponents(final long maxSpans) {
        idToVertexCache = new ConcurrentLinkedHashMap.Builder<Identifier, ComponentVertex>()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
                .maximumWeightedCapacity(maxSpans)  //this setting helps to remove eldest spans from the cache
                .build();
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param span the input argument
     */
    @Override
    public void accept(final Span span) {
        //detect whether the vertex representing the component exists in the map of vertices
        ComponentVertex vertex = new ComponentVertex(span);
        vertex = MoreObjects.firstNonNull(putIfAbsent(span.getComponentName(), vertex), vertex);
        vertex.addInstance(span.getInstanceName());
        //add a new span ID into the cache that provides O(1) search of vertex by its spanID
        if (!span.getSpanID().equals(Identifier.EMPTY))
            idToVertexCache.put(span.getSpanID(), vertex);  //spanID is unqiue so we sure that there is no duplicate key in the map. Eldest span will be removed automatically
        //try to resolve vertex by spanID using cache and create edge between parent/child vertices
        final ComponentVertex parentVertex = idToVertexCache.get(span.getParentSpanID());     //expecting O(1) search of the parent node
        if (parentVertex != null)
            parentVertex.add(vertex);
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
        super.clear();
        idToVertexCache.clear();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        clear();
    }
}
