package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.osgi.framework.BundleContext;

import java.util.Map;

/**
 * Represents a root interface for all charts.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(LineChartOfAttributeValues.class),
        @JsonSubTypes.Type(PanelOfAttributeValues.class),
        @JsonSubTypes.Type(HorizontalBarChartOfAttributeValues.class),
        @JsonSubTypes.Type(PieChartOfAttributeValues.class),
        @JsonSubTypes.Type(VerticalBarChartOfAttributeValues.class)
})
public interface Chart {
    /**
     * Gets user preferences associated with this chart.
     * @return User preferences.
     */
    Map<String, Object> getPreferences();

    /**
     * Sets user preferences associated with this chart.
     * @param value User preferences.
     */
    void setPreferences(final Map<String, Object> value);

    /**
     * Gets name of this chart.
     * @return Chart name.
     */
    String getName();

    /**
     * Sets name of this chart.
     * @param value Chart name. Cannot be {@literal null}.
     */
    void setName(final String value);

    /**
     * Gets number of dimensions.
     * @return Number of dimensions.
     */
    int getDimensions();

    /**
     * Gets configuration of the axis associated with the dimension.
     * @param dimensionIndex Zero-based index of the dimension.
     * @return Axis configuration.
     * @throws IndexOutOfBoundsException Invalid dimension index.
     */
    Axis getAxis(final int dimensionIndex);

    /**
     * Collects chart data.
     * @param context Bundle context. Cannot be {@literal null}.
     * @return Chart data series.
     * @throws Exception The data cannot be collected.
     */
    Iterable<? extends ChartData> collectChartData(final BundleContext context) throws Exception;
}
