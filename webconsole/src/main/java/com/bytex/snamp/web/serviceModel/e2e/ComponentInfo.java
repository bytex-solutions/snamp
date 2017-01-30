package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.ComponentVertex;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents information about component.
 */
public abstract class ComponentInfo {
    private final String name;
    private final String moduleName;

    ComponentInfo(final ComponentVertex vertex) {
        name = vertex.getName();
        moduleName = vertex.getModuleName();
    }

    @JsonProperty("name")
    public final String getName(){
        return name;
    }
}
