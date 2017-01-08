package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Abstract two-dimensional chart of attribute values.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class TwoDimensionalChartOfAttributeValues<X extends Axis, Y extends Axis> extends ChartOfAttributeValues implements TwoDimensionalChart<X, Y> {
    private X axisX;
    private Y axisY;

    @Nonnull
    protected abstract X createDefaultAxisX();

    @Nonnull
    protected abstract Y createDefaultAxisY();

    /**
     * Gets information about X-axis.
     *
     * @return Information about X-axis.
     */
    @Override
    @JsonProperty("X")
    @Nonnull
    public final X getAxisX() {
        if(axisX == null)
            axisX = createDefaultAxisX();
        return axisX;
    }

    public final void setAxisX(@Nonnull final X value){
        axisX = Objects.requireNonNull(value);
    }

    /**
     * Gets information about Y-axis.
     *
     * @return Information about Y-axis.
     */
    @Override
    @JsonProperty("Y")
    @Nonnull
    public final Y getAxisY() {
        if(axisY == null)
            axisY = createDefaultAxisY();
        return axisY;
    }

    public final void setAxisY(@Nonnull final Y value){
        axisY = Objects.requireNonNull(value);
    }
}
