package com.bytex.snamp.moa.topology;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.connector.dsp.notifications.SpanNotification;
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
public class GraphOfComponents extends ConcurrentHashMap<ComponentVertexId, ComponentVertex> implements Consumer<Span>, Stateful {//key in map is a component name
    private static final long serialVersionUID = 2292647118511712487L;
    private final ConcurrentLinkedHashMap<Identifier, ComponentVertex> idToVertexCache; //key is a spanID of the node
    /*
        UC: we have two correlated spans: A => B. Span B can be received earlier than A. In this case cache will not contain a vertex with appropriate spanID
        and span B will be lost. To avoid this we use buffer to save spans like B.
    */
    private final ConcurrentLinkedHashMap<Identifier, Span> spanBuffer; //key is a parentSpanId

    public GraphOfComponents(final long historySize) {
        final int concurrencyLevel = Runtime.getRuntime().availableProcessors() * 2;
        idToVertexCache = new ConcurrentLinkedHashMap.Builder<Identifier, ComponentVertex>()
                .concurrencyLevel(concurrencyLevel)
                .maximumWeightedCapacity(historySize)  //this setting helps to remove eldest spans from the cache
                .build();
        spanBuffer = new ConcurrentLinkedHashMap.Builder<Identifier, Span>()
                .concurrencyLevel(concurrencyLevel)
                .maximumWeightedCapacity(historySize)
                .build();
    }

    protected boolean filterSpan(final Span span){
        return true;
    }

    public final void handleNotification(final SpanNotification span){
        accept(span.getMeasurement());
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param span the input argument
     */
    @Override
    public final void accept(final Span span) {
        //drop span if its source is not allowed
        if(!filterSpan(span))
            return;
        //detect whether the vertex representing the component exists in the map of vertices
        ComponentVertex vertex = new ComponentVertex(span);
        vertex = MoreObjects.firstNonNull(putIfAbsent(vertex.getId(), vertex), vertex);
        vertex.accept(span);
        //add a new span ID into the cache that provides O(1) search of vertex by its spanID
        if (!span.getSpanID().isEmpty()) {
            idToVertexCache.put(span.getSpanID(), vertex);  //spanID is unique so we sure that there is no duplicate key in the map. Eldest span will be removed automatically
            //correlate buffered span with newly supplied span
            final Span childSpan = spanBuffer.remove(span.getSpanID());
            if (childSpan != null)
                accept(childSpan);
        }
        //try to resolve vertex by spanID using cache and create edge between parent/child vertices
        if (!span.getParentSpanID().isEmpty()) {
            final ComponentVertex parentVertex = idToVertexCache.remove(span.getParentSpanID());     //expecting O(1) removal of the parent vertex
            if (parentVertex == null)
                spanBuffer.put(span.getParentSpanID(), span);
            else
                parentVertex.add(vertex);
        }
    }

    public final ComponentVertex get(final String componentName){
        return get(componentName, "");
    }

    public final ComponentVertex get(final String componentName, final String moduleName){
        return get(new ComponentVertexId(componentName, moduleName));
    }

    public final boolean remove(final String componentName) {
        final boolean success = keySet().removeIf(id -> id.getComponentName().equals(componentName));
        idToVertexCache.values().removeIf(entry -> entry.getName().equals(componentName));
        return success;
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public final void clear() {
        super.clear();
        idToVertexCache.clear();
        spanBuffer.clear();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        clear();
    }
}
