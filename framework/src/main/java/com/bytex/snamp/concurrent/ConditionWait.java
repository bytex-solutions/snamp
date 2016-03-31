package com.bytex.snamp.concurrent;

import com.bytex.snamp.TimeSpan;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Represents spin wait based on the periodic condition check.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public abstract class ConditionWait extends SpinWait<Object> {

    protected ConditionWait(final TimeSpan spinDelay) {
        super(spinDelay);
    }

    protected ConditionWait() {
        this(DEFAULT_SPIN_DELAY);
    }

    /**
     * Do conditional check.
     *
     * @return {@literal true} to change the state of this spin wait to signalled; otherwise, {@literal false}.
     * @throws Throwable Internal condition check error.
     */
    protected abstract boolean checkCondition() throws Throwable;

    @Override
    protected final Object spin() throws Throwable {
        return checkCondition() ? this : null;
    }

    public static <T> ConditionWait create(final Predicate<? super T> predicate, final Supplier<? extends T> stateProvider){
        return new ConditionWait() {
            @Override
            protected boolean checkCondition() {
                return predicate.apply(stateProvider.get());
            }
        };
    }

    public static <T> ConditionWait create(final Predicate<? super T> predicate, final T state){
        return create(predicate, Suppliers.ofInstance(state));
    }
}
