package com.bytex.snamp.instrumentation.reporters.util;

import com.bytex.snamp.instrumentation.measurements.Measurement;

/**
 * Represents utility interface for measurements buffer used as temporary store when reporter has connection problems.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface MeasurementBuffer {
    /**
     * Represents how the measurement was handled by this buffer.
     */
    enum PlacementResult{
        /**
         * Measurement is saved successfully.
         */
        SUCCESS,
        /**
         * Oldest measurement was replaced with newly supplied.
         */
        DROP_OLD_MEASUREMENT,

        /**
         * A new measurement cannot be placed into buffer.
         */
        NOT_ENOUGH_SPACE
    }

    /**
     * Saves measurement into buffer.
     * @param measurement A measurement to store in the buffer.
     * @return A result of measurement handling.
     */
    PlacementResult place(final Measurement measurement);

    /**
     * Removes a single measurement from this buffer.
     * @return Measurement instance; or {@literal null}, if this buffer is empty.
     */
    Measurement remove();

    /**
     * Removes all measurements.
     */
    void clear();

    /**
     * Gets number of measurements in this buffer.
     * @return Number of measurements in this buffer.
     */
    int size();
}
