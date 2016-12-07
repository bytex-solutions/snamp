package com.bytex.snamp.instrumentation.reporters.util;

import com.bytex.snamp.instrumentation.measurements.Measurement;

import java.lang.ref.SoftReference;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents measurement buffer that can save as many measurements as possible (limited by memory).
 * <p>
 *     This class provides may cause a full GC when memory is occupied by buffered measurements.
 *     In this case all buffered measurement will be GCed in respect to program's fault-tolerance.
 *     This happen because the buffer stores soft reference to the list of saved measurements.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SoftMeasurementBuffer implements MeasurementBuffer {
    private volatile SoftReference<BlockingQueue<Measurement>> buffer;

    private synchronized BlockingQueue<Measurement> getQueueSync(){
        BlockingQueue<Measurement> queue;
        if(buffer == null || (queue = buffer.get()) == null)
            buffer = new SoftReference<>(queue = new LinkedBlockingQueue<>());
        return queue;
    }

    private BlockingQueue<Measurement> getQueue() {
        BlockingQueue<Measurement> queue;
        SoftReference<BlockingQueue<Measurement>> buffer = this.buffer;
        if (buffer == null || (queue = buffer.get()) == null)
            queue = getQueueSync();
        return queue;
    }

    /**
     * Saves measurement into buffer.
     *
     * @param measurement A measurement to store in the buffer.
     * @return A result of measurement handling.
     */
    @Override
    public PlacementResult place(final Measurement measurement) {
        return getQueue().offer(measurement) ? PlacementResult.SUCCESS : PlacementResult.NOT_ENOUGH_SPACE;
    }

    /**
     * Removes a single measurement from this buffer.
     *
     * @return Measurement instance; or {@literal null}, if this buffer is empty.
     */
    @Override
    public Measurement remove() {
        return getQueue().poll();
    }

    /**
     * Removes all measurements.
     */
    @Override
    public synchronized void clear() {
        if(buffer != null){
            final BlockingQueue<Measurement> queue = buffer.get();
            if(queue != null)
                queue.clear();
            buffer.clear(); //help GC
        }
        buffer = null;
    }

    /**
     * Gets number of measurements in this buffer.
     *
     * @return Number of measurements in this buffer.
     */
    @Override
    public int size() {
        return getQueue().size();
    }
}
