package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

/**
 * Represents abstract axis definition.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@JsonSubTypes({
        @JsonSubTypes.Type(ChronoAxis.class),
        @JsonSubTypes.Type(AttributeValueAxis.class),
        @JsonSubTypes.Type(ResourceNameAxis.class),
        @JsonSubTypes.Type(HealthStatusAxis.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public abstract class Axis {
    private String name;

    Axis(){
        name = "";
    }

    /**
     * Gets name of this axis.
     * @return Name of this axis.
     */
    @JsonProperty("name")
    public final String getName(){
        return name;
    }

    /**
     * Sets name of this axis.
     * @param value Axis name.
     */
    public final void setName(final String value){
        name = value;
    }

    /**
     * Gets unit of measurement associated with this axis.
     * @return Unit of measurement associated with this axis.
     */
    public abstract String getUOM();
}
