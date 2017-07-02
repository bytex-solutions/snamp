package com.bytex.snamp.web.serviceModel.e2e;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

/**
 * Represents E2E view based on the specified component.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractComponentSpecificView extends MatrixBasedView {
    private String rootComponent = "";

    @JsonProperty("rootComponent")
    public final String getTargetComponent(){
        return rootComponent;
    }

    public final void setTargetComponent(final String value){
        rootComponent = Objects.requireNonNull(value);
    }


}
