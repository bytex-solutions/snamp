package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents data model of dashboard with charts.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("dashboardOfCharts")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public final class Dashboard {
    private final List<Chart> charts = new LinkedList<>();

    @JsonProperty("charts")
    public List<Chart> getCharts(){
        return charts;
    }

    public void setCharts(final Collection<Chart> value) {
        charts.clear();
        charts.addAll(value);
    }

    void addChart(final Chart value){
        charts.add(value);
    }
}
