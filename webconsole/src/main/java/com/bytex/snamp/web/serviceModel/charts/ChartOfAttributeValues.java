package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.ArrayUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ChartOfAttributeValues extends AbstractChart {
    private String[] instances; //empty for all instances
    private String componentType;
    private String chartName;

    public ChartOfAttributeValues() {
        instances = ArrayUtils.emptyArray(String[].class);
        componentType = chartName = "";
    }

    @JsonProperty("component")
    public final String getComponentType() {
        return componentType;
    }

    public final void setComponentType(final String value) {
        componentType = Objects.requireNonNull(value);
    }

    @JsonProperty("instances")
    public final String[] getInstances() {
        return instances;
    }

    public final void setInstances(final String... value) {
        instances = Objects.requireNonNull(value).clone();
    }

    @JsonIgnore
    public final boolean isAnyInstance() {
        return instances.length == 0;
    }
}
