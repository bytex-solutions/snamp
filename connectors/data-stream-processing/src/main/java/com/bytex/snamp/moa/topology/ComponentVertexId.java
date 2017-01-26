package com.bytex.snamp.moa.topology;

import com.bytex.snamp.instrumentation.measurements.Span;

import java.util.Objects;

/**
 * Represents identifier of the vertex.
 * <p>
 *     This identifier is constructed from component name and name of its internal module.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ComponentVertexId {
    private final String componentName;
    private final String moduleName;

    ComponentVertexId(final Span span){
        componentName = span.getComponentName();
        moduleName = span.getModuleName();
    }

    public String getComponentName(){
        return componentName;
    }

    public String getModuleName(){
        return moduleName;
    }

    boolean represents(final Span span){
        return span.getComponentName().equals(componentName) && span.getModuleName().equals(moduleName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(componentName, moduleName);
    }

    private boolean equals(final ComponentVertexId other){
        return other.componentName.equals(componentName) && other.moduleName.equals(moduleName);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ComponentVertexId && equals((ComponentVertexId) other);
    }

    @Override
    public String toString() {
        return moduleName.isEmpty() ? componentName : componentName + '/' + moduleName;
    }
}
