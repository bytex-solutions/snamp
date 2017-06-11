package com.bytex.snamp.moa;

import javax.annotation.Nonnull;
import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class TimeBasedDoubleEMA extends DoubleEMA {
    private static final long serialVersionUID = -4901268958873208221L;
    private final double alpha;

    public TimeBasedDoubleEMA(final Duration meanLifetime,
                              final Duration measurementInterval) {
        alpha = computeAlpha(meanLifetime, measurementInterval);
    }

    private TimeBasedDoubleEMA(final TimeBasedDoubleEMA other){
        super(other);
        alpha = other.alpha;
    }

    @Override
    @Nonnull
    public TimeBasedDoubleEMA clone() {
        return new TimeBasedDoubleEMA(this);
    }

    @Override
    double getDecay() {
        return alpha;
    }
}
