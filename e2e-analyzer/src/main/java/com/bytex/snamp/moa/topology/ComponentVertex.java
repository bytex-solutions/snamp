package com.bytex.snamp.moa.topology;

import com.bytex.snamp.connector.metrics.Arrivals;
import com.bytex.snamp.connector.metrics.ArrivalsRecorder;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Represents component as a vertex in graph.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ComponentVertex extends ConcurrentLinkedQueue<ComponentVertex> implements Consumer<Span> {
    private static final long serialVersionUID = -8818600618319266377L;
    private final ComponentVertexIdentity id;
    private final Set<String> instances;
    private final ArrivalsRecorder arrivals;

    ComponentVertex(final Span span) {
        id = new ComponentVertexIdentity(span);
        final ConcurrentLinkedHashMap<String, Boolean> instances = new ConcurrentLinkedHashMap.Builder<String, Boolean>()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
                //at maximum we can hold no more than 100 instances of the same component
                //we expected that this capacity is enough for typical clusters
                .maximumWeightedCapacity(100)
                .build();
        this.instances = Collections.newSetFromMap(instances);
        this.instances.add(span.getInstanceName());
        this.arrivals = new ArrivalsRecorder(id.toString());
    }

    private void handleSpan(final Span span) {
        instances.add(span.getInstanceName());
        arrivals.accept(span.convertTo(Duration.class));
        arrivals.setChannels(instances.size());
    }

    @Override
    public void accept(final Span span) {
        if (id.represents(span))
            handleSpan(span);
    }

    void removeChild(final String componentName){
        removeIf(vertex -> vertex.getName().equals(componentName));
    }

    /**
     * Gets analytics about arrivals
     *
     * @return Analytics about arrivals.
     */
    public Arrivals getArrivals() {
        return arrivals;
    }

    /**
     * Gets read-only list of instances.
     * @return Read-only list of instances.
     */
    public Set<String> getInstances(){
        return Collections.unmodifiableSet(instances);
    }

    /**
     * Gets identifier of this vertex.
     * @return Identifier of this vertex.
     */
    public ComponentVertexIdentity getIdentity(){
        return id;
    }

    public String getName(){
        return id.getComponentName();
    }

    public String getModuleName(){
        return id.getModuleName();
    }

    @Override
    public String toString() {
        return String.format("component=%s, module=%s, instances=%s, children=%s", id.getComponentName(), id.getModuleName(), instances, size());
    }
}
