package com.bytex.snamp.instrumentation.reporters.util;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Represents capacity-bounded buffer for measurements.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class BoundedMeasurementBuffer extends ArrayBlockingQueue<Measurement> implements MeasurementBuffer {
    private static final long serialVersionUID = 7887492090334489430L;

    /**
     * Initializes a new buffer for measurements.
     * @param capacity the capacity of this buffer
     * @throws IllegalArgumentException if {@code capacity < 1}
     */
    public BoundedMeasurementBuffer(final int capacity) {
        super(capacity);
    }

    /**
     * Saves measurement into buffer.
     * <p/>
     *     The default implementation always drops the old measurements and never
     *     returns {@link PlacementResult#NOT_ENOUGH_SPACE}
     *
     * @param measurement A measurement to store in the buffer.
     * @return A result of measurement handling.
     */
    @Override
    public PlacementResult place(final Measurement measurement) {
        PlacementResult result = PlacementResult.SUCCESS;
        while (!offer(measurement)){
            poll();
            result = PlacementResult.DROP_OLD_MEASUREMENT;
        }
        return result;
    }

    @Override
    public Measurement remove(){
        return poll();
    }
}
