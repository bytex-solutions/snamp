package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.ArrayUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

/**
 * Represents definition of the chart.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ChartSettings {
    private String[] instances; //empty for all instances
    private String componentType;
    private String attributeName;

    public ChartSettings(){
        instances = ArrayUtils.emptyArray(String[].class);
        componentType = "";
    }

    @JsonProperty("component")
    public String getComponentType(){
        return componentType;
    }

    public void setComponentType(final String value){
        componentType = Objects.requireNonNull(value);
    }

    @JsonProperty("instances")
    public String[] getInstances(){
        return instances;
    }

    public void setInstances(final String... value){
        instances = Objects.requireNonNull(value).clone();
    }

    @JsonIgnore
    public boolean isAnyInstance(){
        return instances.length == 0;
    }
}
