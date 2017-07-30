package com.bytex.snamp.instrumentation.reporters.util;

import com.bytex.snamp.instrumentation.measurements.Measurement;

/**
 * Represents measurement buffer that can't save any measurement.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class EmptyMeasurementBuffer implements MeasurementBuffer {
    /**
     * Gets singleton instance of this buffer.
     */
    public static final EmptyMeasurementBuffer INSTANCE = new EmptyMeasurementBuffer();

    private EmptyMeasurementBuffer(){

    }

    /**
     * Saves measurement into buffer.
     *
     * @param measurement A measurement to store in the buffer.
     * @return A result of measurement handling.
     */
    @Override
    public PlacementResult place(final Measurement measurement) {
        return PlacementResult.NOT_ENOUGH_SPACE;
    }

    /**
     * Removes all measurements.
     */
    @Override
    public void clear() {

    }

    /**
     * Removes a single measurement from this buffer.
     *
     * @return Measurement instance; or {@literal null}, if this buffer is empty.
     */
    @Override
    public Measurement remove() {
        return null;
    }

    /**
     * Gets number of measurements in this buffer.
     *
     * @return Number of measurements in this buffer.
     */
    @Override
    public int size() {
        return 0;
    }
}
