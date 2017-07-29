package com.bytex.snamp.moa.topology;

import com.bytex.snamp.connector.metrics.Arrivals;
import com.bytex.snamp.connector.metrics.ArrivalsRecorder;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Represents component as a vertex in graph.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ComponentVertex extends ConcurrentLinkedQueue<ComponentVertex> implements Consumer<Span> {
    private static final long serialVersionUID = -8818600618319266377L;
    private final ComponentVertexIdentity id;
    private final Set<String> instances;
    private final ArrivalsRecorder arrivals;
    private final AtomicLong lastUpdate; //time since last update

    ComponentVertex(final Span span) {
        id = new ComponentVertexIdentity(span);
        final ConcurrentLinkedHashMap<String, Boolean> instances = new ConcurrentLinkedHashMap.Builder<String, Boolean>()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
                //at maximum we can hold no more than 100 instances of the same component
                //we expected that this capacity is enough for typical clusters
                .maximumWeightedCapacity(100)
                .build();
        this.instances = Collections.newSetFromMap(instances);
        this.arrivals = new ArrivalsRecorder(id.toString());
        this.lastUpdate = new AtomicLong(0L);
        handleSpan(span);
    }

    private void handleSpan(final Span span) {
        lastUpdate.set(System.nanoTime());
        instances.add(span.getInstanceName());
        arrivals.accept(span.convertTo(Duration.class));
        arrivals.setChannels(instances.size());
    }

    /**
     * Gets age of the last measurement saved to this vertex.
     * @return Age of the last measurement.
     */
    public Duration getAge() {
        return Duration.ofNanos(System.nanoTime() - lastUpdate.get());
    }

    /**
     * Checks time of last measurement associated with this vertex and
     * specified shelf time.
     * @param shelfLife Shelf time.
     * @return {@literal true}, if age of the last measurement is not greater than specified shelf time; otherwise, {@literal false}.
     */
    public boolean checkAge(final Duration shelfLife) {
        return shelfLife == null || getAge().compareTo(shelfLife) <= 0;
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
