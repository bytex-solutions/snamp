package com.bytex.snamp.tracer;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.util.Objects;

/**
 * Represents abstract POJO for all measurements.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({@JsonSubTypes.Type(IntegerMeasurement.class)})
public abstract class Measurement {
    private String instanceName;
    private String componentName;
    private String message;

    Measurement(){
        instanceName = componentName = message = "";
    }

    @JsonProperty("c")
    public final String getComponentName(){
        return componentName;
    }

    public final void setComponentName(final String value){
        componentName = Objects.requireNonNull(value);
    }

    @JsonProperty("i")
    public final String getInstanceName(){
        return instanceName;
    }

    public final void setInstanceName(final String value){
        instanceName = Objects.requireNonNull(value);
    }

    @JsonProperty("m")
    public final String getMessage(){
        return message;
    }

    public final void setMessage(final String value){
        message = Objects.requireNonNull(value);
    }
}
