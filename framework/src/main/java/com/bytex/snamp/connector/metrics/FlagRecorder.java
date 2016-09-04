package com.bytex.snamp.connector.metrics;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents implementation of {@link Flag}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FlagRecorder extends AbstractMetric implements Flag {
    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private final AtomicInteger value;

    public FlagRecorder(final String name) {
        super(name);
        value = new AtomicInteger(toInt(false));
    }

    private static boolean fromInt(final int value){
        return value == TRUE;
    }

    private static int toInt(final boolean value){
        return value ? TRUE : FALSE;
    }

    public void update(final boolean value){
        this.value.set(toInt(value));
    }

    public void inverse() {
        int next, prev;
        do {
            prev = value.get();
            next = toInt(!fromInt(prev));
        } while (!value.compareAndSet(prev, next));
    }

    public void or(final boolean value){
        this.value.accumulateAndGet(toInt(value), (current, provided) -> toInt(fromInt(current) | fromInt(provided)));
    }

    public void and(final boolean value){
        this.value.accumulateAndGet(toInt(value), (current, provided) -> toInt(fromInt(current) & fromInt(provided)));
    }

    public void xor(final boolean value){
        this.value.accumulateAndGet(toInt(value), (current, provided) -> toInt(fromInt(current) ^ fromInt(provided)));
    }

    /**
     * Resets all metrics.
     */
    @Override
    public void reset() {
        this.value.set(toInt(false));
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public boolean getAsBoolean() {
        return fromInt(value.get());
    }
}
