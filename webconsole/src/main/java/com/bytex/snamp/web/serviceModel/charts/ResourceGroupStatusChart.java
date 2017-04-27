package com.bytex.snamp.web.serviceModel.charts;

import org.osgi.framework.BundleContext;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceGroupStatusChart extends AbstractChart {
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

    /**
     * Collects chart data.
     *
     * @param context Bundle context. Cannot be {@literal null}.
     * @return Chart data series.
     * @throws Exception The data cannot be collected.
     */
    @Override
    public Iterable<? extends ChartData> collectChartData(final BundleContext context) throws Exception {
        return null;
    }
}
