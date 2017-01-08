package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Represents abstract class for all charts.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractChart implements Chart {
    private String chartName;

    AbstractChart(){
        chartName = "";
    }

    @Override
    @JsonProperty("name")
    public final String getName() {
        return chartName;
    }

    @Override
    public void setName(final String value) {
        chartName = value;
    }
}
