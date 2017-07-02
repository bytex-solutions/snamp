package com.bytex.snamp.moa.topology;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.instrumentation.measurements.jmx.SpanNotification;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Provides analysis of topology between components based on stream of {@link Span}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public class GraphOfComponents extends ConcurrentHashMap<ComponentVertexIdentity, ComponentVertex> implements Consumer<Span>, Stateful {//key in map is a component name
    private static final long serialVersionUID = 2292647118511712487L;

    private static final class SpanQueue extends ArrayBlockingQueue<Span> {
        private static final long serialVersionUID = -5482006202965410683L;

        SpanQueue() {
            super(20);
        }
    }

    private final ConcurrentLinkedHashMap<Identifier, ComponentVertex> idToVertexCache; //key is a spanID of the node
    /*
        UC: we have two correlated spans: A => B. Span B can be received earlier than A. In this case cache will not contain a vertex with appropriate spanID
        and span B will be lost. To avoid this we use buffer to save spans like B.
    */
    private final ConcurrentLinkedHashMap<Identifier, SpanQueue> spanBuffer; //key is a parentSpanId

    public GraphOfComponents(final long historySize,
                             final int concurrencyLevel){
        idToVertexCache = new ConcurrentLinkedHashMap.Builder<Identifier, ComponentVertex>()
                .concurrencyLevel(concurrencyLevel)
                .maximumWeightedCapacity(historySize)  //this setting helps to remove eldest spans from the cache
                .build();
        spanBuffer = new ConcurrentLinkedHashMap.Builder<Identifier, SpanQueue>()
                .concurrencyLevel(concurrencyLevel)
                .maximumWeightedCapacity(historySize)
                .build();
    }

    public GraphOfComponents(final long historySize) {
        this(historySize, ThreadPoolConfiguration.DEFAULT_MAX_POOL_SIZE);
    }

    protected boolean filterSpan(final Span span){
        return true;
    }

    public final void accept(final SpanNotification span) {
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
        vertex = firstNonNull(putIfAbsent(vertex.getIdentity(), vertex), vertex);
        vertex.accept(span);
        //add a new span ID into the cache that provides O(1) search of vertex by its spanID
        if (!span.getSpanID().isEmpty()) {
            idToVertexCache.put(span.getSpanID(), vertex);  //spanID is unique so we sure that there is no duplicate key in the map. Eldest span will be removed automatically
            //correlate buffered span with newly supplied span
            final SpanQueue children = spanBuffer.remove(span.getSpanID());
            if (children != null) {
                children.forEach(this);
                children.clear();   //help GC
            }
        }
        //try to resolve vertex by spanID using cache and create edge between parent/child vertices
        if (!span.getParentSpanID().isEmpty()) {
            final ComponentVertex parentVertex = idToVertexCache.get(span.getParentSpanID());     //expecting O(1) access to the parent vertex
            if (parentVertex == null) {
                SpanQueue queue = new SpanQueue();
                queue = firstNonNull(spanBuffer.putIfAbsent(span.getParentSpanID(), queue), queue);
                queue.offer(span);
            }
            else
                parentVertex.add(vertex);
        }
    }

    public final ComponentVertex get(final String componentName){
        return get(componentName, "");
    }

    public final ComponentVertex get(final String componentName, final String moduleName){
        return get(new ComponentVertexIdentity(componentName, moduleName));
    }

    public boolean remove(final String componentName) {
        final boolean success = keySet().removeIf(id -> id.getComponentName().equals(componentName));
        idToVertexCache.values().removeIf(entry -> entry.getName().equals(componentName));
        spanBuffer.clear();
        values().forEach(vertex -> vertex.removeChild(componentName));
        return success;
    }

    public final <E extends Throwable> void forEach(final Acceptor<? super ComponentVertex, E> visitor) throws E {
        for (final ComponentVertex vertex : values())
            visitor.accept(vertex);
    }

    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public final void clear() {
        idToVertexCache.clear();
        spanBuffer.clear();
        super.clear();
    }

    /**
     * Resets internal state of the object.
     */
    @Override
    public void reset() {
        clear();
    }
}
