package com.bytex.snamp.moa.topology;

import com.bytex.snamp.instrumentation.measurements.Span;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents component as a vertex in graph.
 * This class cannot be inherited or instantiated directly from your code.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ComponentVertex extends ConcurrentLinkedQueue<ComponentVertex> {
    private static final long serialVersionUID = -8818600618319266377L;
    private final String componentName;
    private final Set<String> instances;

    ComponentVertex(final Span span) {
        componentName = span.getComponentName();
        instances = Collections.newSetFromMap(new ConcurrentHashMap<>());
        instances.add(span.getInstanceName());
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
