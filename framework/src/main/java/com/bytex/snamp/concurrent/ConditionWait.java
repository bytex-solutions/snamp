package com.bytex.snamp.concurrent;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents spin wait based on the periodic condition check.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public final class ConditionWait extends SpinWait<Boolean> {

    private ConditionWait(final BooleanSupplier spin, final Duration spinDelay) {
        super(() -> spin.getAsBoolean() ? Boolean.TRUE : null, spinDelay);
    }

    public static ConditionWait create(final BooleanSupplier condition){
        return new ConditionWait(condition, DEFAULT_SPIN_DELAY);
    }

    public static <T> ConditionWait create(final Supplier<? extends T> stateProvider, final Predicate<? super T> predicate){
        return create(() -> predicate.test(stateProvider.get()));
    }

    public static <T> ConditionWait create(final T state, final Predicate<? super T> predicate) {
        return create(() -> predicate.test(state));
    }
}
