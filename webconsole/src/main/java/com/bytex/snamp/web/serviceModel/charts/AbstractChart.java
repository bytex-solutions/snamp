package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents abstract class for all charts.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractChart implements Chart {
    private final Map<String, Object> preferences;
    private String chartName;

    AbstractChart(){
        chartName = "";
        preferences = new HashMap<>();
    }

    @Override
    @JsonProperty("name")
    public final String getName() {
        return chartName;
    }

    @Override
    @JsonProperty("preferences")
    public final Map<String, Object> getPreferences() {
        return preferences;
    }

    @Override
    public final void setPreferences(final Map<String, Object> value) {
        preferences.clear();
        preferences.putAll(value);
    }

    /**
     * Gets number of dimensions.
     *
     * @return Number of dimensions.
     */
    @Override
    public int getDimensions() {
        return 0;
    }

    @Override
    public final void setName(final String value) {
        chartName = value;
    }
}
