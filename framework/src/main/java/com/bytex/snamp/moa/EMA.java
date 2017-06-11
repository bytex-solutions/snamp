package com.bytex.snamp.moa;

import java.time.Duration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class EMA extends Average {
    private static final long serialVersionUID = -6446646338156742697L;

    private static double computeAlpha(final double meanLifetimeNanos, final double measurementIntervalNanos){
        return 1 - Math.exp(-measurementIntervalNanos / meanLifetimeNanos);
    }

    static double computeAlpha(final Duration meanLifetime,
                                       final Duration measurementInterval) {
        return computeAlpha(meanLifetime.toNanos(), measurementInterval.toNanos());
    }
}
