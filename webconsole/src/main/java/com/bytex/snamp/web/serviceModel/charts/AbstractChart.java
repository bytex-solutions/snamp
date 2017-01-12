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
    private final Map<String, Object> settings;
    private String chartName;

    AbstractChart(){
        chartName = "";
        settings = new HashMap<>();
    }

    @Override
    @JsonProperty("name")
    public final String getName() {
        return chartName;
    }

    @Override
    @JsonProperty("preferences")
    public final Map<String, Object> getPreferences() {
        return settings;
    }

    @Override
    public final void setPreferences(final Map<String, Object> value) {
        settings.clear();
        settings.putAll(value);
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

    /**
     * Gets configuration of the axis associated with the dimension.
     *
     * @param dimensionIndex Zero-based index of the dimension.
     * @return Axis configuration.
     * @throws IndexOutOfBoundsException Invalid dimension index.
     */
    @Override
    public Axis getAxis(final int dimensionIndex) {
        return null;
    }

    @Override
    public void setName(final String value) {
        chartName = value;
    }
}
