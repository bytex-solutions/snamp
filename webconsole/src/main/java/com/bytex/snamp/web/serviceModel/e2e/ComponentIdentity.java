package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

/**
 * Represents information about component.
 */
public class ComponentIdentity {
    private final String name;
    private final String moduleName;

    ComponentIdentity(final ComponentVertex vertex) {
        name = vertex.getName();
        moduleName = vertex.getModuleName();
    }

    @JsonProperty("name")
    public final String getName(){
        return name;
    }

    public final String getModuleName(){
        return moduleName;
    }

    final boolean represents(final ComponentVertex vertex){
        return vertex.getName().equals(name) && vertex.getModuleName().equals(moduleName);
    }

    private boolean equals(final ComponentIdentity other){
        return other.name.equals(name) && other.moduleName.equals(moduleName);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ComponentIdentity && equals((ComponentIdentity) other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, moduleName);
    }

    @Override
    public String toString() {
        return String.format("component=%s, module=%s", name, moduleName);
    }
}
