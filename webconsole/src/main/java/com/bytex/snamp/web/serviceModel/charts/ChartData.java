package com.bytex.snamp.web.serviceModel.charts;

/**
 * Represents chart data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ChartData {
    /**
     * Gets data for the specified dimension.
     * @param dimension Zero-based dimension index.
     * @return Data for the specified dimension.
     * @throws IndexOutOfBoundsException Invalid dimension index.
     */
    Object getData(final int dimension);
}
