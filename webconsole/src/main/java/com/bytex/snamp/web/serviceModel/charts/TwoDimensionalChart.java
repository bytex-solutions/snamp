package com.bytex.snamp.web.serviceModel.charts;

import javax.annotation.Nonnull;

/**
 * Represents two-dimensional chart.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface TwoDimensionalChart<X extends Axis, Y extends Axis> extends Chart {
    /**
     * Gets information about X-axis.
     * @return Information about X-axis.
     */
    @Nonnull
    X getAxisX();

    /**
     * Gets information about Y-axis.
     * @return Information about Y-axis.
     */
    @Nonnull
    Y getAxisY();

    /**
     * Gets number of dimensions.
     *
     * @return Number of dimensions.
     */
    @Override
    default int getDimensions() {
        return 2;
    }

    /**
     * Gets configuration of the axis associated with the dimension.
     *
     * @param dimensionIndex Zero-based index of the dimension.
     * @return Axis configuration.
     * @throws IndexOutOfBoundsException Invalid dimension index.
     */
    @Override
    default Axis getAxis(final int dimensionIndex) {
        switch (dimensionIndex){
            case 0:
                return getAxisX();
            case 1:
                return getAxisY();
            default:
                throw new IndexOutOfBoundsException();
        }
    }
}
