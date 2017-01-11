package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.ArrayUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.*;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class ChartOfAttributeValues extends AbstractChart {
    private final Set<String> instances; //empty for all instances
    private String componentType;

    public ChartOfAttributeValues() {
        instances = new HashSet<>();
        componentType = "";
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
        return instances.toArray(ArrayUtils.emptyArray(String[].class));
    }

    public final void setInstances(final String... value) {
        instances.clear();
        Collections.addAll(instances, value);
    }

    @JsonIgnore
    final boolean hasInstance(final String instanceName){
        return instances.isEmpty() || instances.contains(instanceName);
    }

    abstract Optional<? extends AttributeChartData> createChartData(final String instanceName, final Attribute attribute);

    final void fillCharData(final String instanceName, final AttributeList attributes, final Map<String, ChartData> output) {
        final String chartName = getName();
        for (final Attribute attribute : attributes.asList())
            createChartData(instanceName, attribute)
                    .ifPresent(chartData -> output.put(chartName, chartData));
    }
}
