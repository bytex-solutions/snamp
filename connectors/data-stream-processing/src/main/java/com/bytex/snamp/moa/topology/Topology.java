package com.bytex.snamp.moa.topology;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Provides analysis of topology between components based on stream of {@link Span}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class Topology implements Consumer<Span>, Stateful {
    public static final class Vertex extends ConcurrentLinkedQueue<Vertex> {
        private static final long serialVersionUID = -8818600618319266377L;
        private final String componentName;
        private final Set<String> instances;

        private Vertex(final Span span){
            componentName = span.getComponentName();
            instances = Sets.newConcurrentHashSet();
            instances.add(span.getInstanceName());
        }

        public String getComponentName(){
            return componentName;
        }

        private void addInstance(final String instanceName){
            instances.add(instanceName);
        }
    }

    private final ConcurrentLinkedHashMap<Identifier, Vertex> idToVerticesCache; //key is a spanID of the node
    private final ConcurrentMap<String, Vertex> vertices;   //key is a component name

    public Topology(final int maxSpans) {
        idToVerticesCache = new ConcurrentLinkedHashMap.Builder<Identifier, Vertex>()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
                .maximumWeightedCapacity(maxSpans)
                .build();
        vertices = new ConcurrentHashMap<>();
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param span the input argument
     */
    @Override
    public void accept(final Span span) {
        //detect whether the vertex representing the component exists in the map of vertices
        Vertex vertex = new Vertex(span);
        vertex = MoreObjects.firstNonNull(vertices.putIfAbsent(span.getComponentName(), vertex), vertex);
        vertex.addInstance(span.getInstanceName());
        //add a new span ID into the cache that provides O(1) search of vertex by its spanID
        if (!span.getSpanID().equals(Identifier.EMPTY))
            idToVerticesCache.put(span.getSpanID(), vertex);  //spanID is unqiue so we sure that there is no duplicate key in the map
        //try to resolve vertex by spanID using cache and create edge between parent/child vertices
        final Vertex parentVertex = idToVerticesCache.get(span.getParentSpanID());     //expecting O(1) search of the parent node
        if (parentVertex != null)
            parentVertex.add(vertex);
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        vertices.clear();
        idToVerticesCache.clear();
    }
}
