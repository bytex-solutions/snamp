package com.bytex.snamp.moa.topology;

import com.bytex.snamp.connector.metrics.Arrivals;
import com.bytex.snamp.connector.metrics.ArrivalsRecorder;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
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
    private final String componentName;
    private final Set<String> instances;
    private final ArrivalsRecorder arrivals;

    ComponentVertex(final Span span) {
        componentName = span.getComponentName();
        final ConcurrentLinkedHashMap<String, Boolean> instances = new ConcurrentLinkedHashMap.Builder<String, Boolean>()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
                //at maximum we can hold no more than 100 instances of the same component
                //we expected that this capacity is enough for typical clusters
                .maximumWeightedCapacity(100)
                .build();
        this.instances = Collections.newSetFromMap(instances);
        this.instances.add(span.getInstanceName());
        this.arrivals = new ArrivalsRecorder(componentName);
    }

    private void handleSpan(final Span span){
        arrivals.accept(span.convertTo(Duration.class));
    }

    @Override
    public void accept(final Span span) {
        if(Objects.equals(getComponentName(), span.getComponentName()))
            handleSpan(span);
    }

    /**
     * Gets analytics about arrivals
     * @return Analytics about arrivals.
     */
    public Arrivals getArrivals(){
        return arrivals;
    }

    /**
     * Gets component name associated with the vertex.
     * @return The component name associated with the vertex.
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Gets number of instances of the component associated with this vertex.
     * @return Number of instances of the component.
     */
    public int getInstances(){
        return instances.size();
    }

    void addInstance(final String instanceName) {
        instances.add(instanceName);
    }
}
