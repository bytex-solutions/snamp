package com.bytex.snamp;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.bytex.snamp.internal.annotations.ThreadSafe;

import java.util.Objects;

/**
 * Represents alternative {@code switch} construction that uses any input type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class Switch<I, O> implements Function<I, O> {
    private static final class SwitchNode<I, O> {
        private final Predicate<? super I> predicate;
        private final Function<? super I, ? extends O> transformer;
        private SwitchNode<I, O> nextNode = null;

        private SwitchNode(final Predicate<? super I> condition,
                           final Function<? super I, ? extends O> action) {
            this.predicate = Objects.requireNonNull(condition, "condition is null.");
            this.transformer = Objects.requireNonNull(action, "action is null.");
        }

        private SwitchNode<I, O> append(final Predicate<? super I> condition,
                                        final Function<? super I, ? extends O> action) {
            return this.nextNode = new SwitchNode<>(condition, action);
        }
    }

    private SwitchNode<I, O> first;
    private SwitchNode<I, O> last;
    private Function<? super I, ? extends O> defaultCase;

    /**
     * Initializes a new empty switch-block.
     */
    public Switch(){
        first = last = null;
        defaultCase = null;
    }

    /**
     * Adds a new case for this switch.
     * @param condition The condition evaluator.
     * @param action The action performed on input value.
     * @return This object.
     */
    @ThreadSafe(false)
    public final Switch<I, O> addCase(final Predicate<? super I> condition,
                        final Function<? super I, ? extends O> action){
        if(first == null)
            first = last = new SwitchNode<>(condition, action);
        else last = last.append(condition, action);
        return this;
    }

    @ThreadSafe(false)
    public final Switch<I, O> equals(final I value,
                               final Function<? super I, ? extends O> action) {
        return addCase(Predicates.equalTo(value), action);
    }

    @ThreadSafe(false)
    public final Switch<I, O> equals(final I value,
                               final O output) {
        return equals(value, Functions.constant(output));
    }

    @ThreadSafe(false)
    public final Switch<I, O> theSame(final I value,
                                final Function<? super I, ? extends O> action){
        return addCase(new Predicate<I>() {
            @Override
            public boolean apply(final I other) {
                return System.identityHashCode(value) == System.identityHashCode(other);
            }
        }, action);
    }

    @ThreadSafe(false)
    public final Switch<I, O> theSame(final I value,
                                final O output) {
        return theSame(value, Functions.constant(output));
    }

    /**
     * Assigns default case to this switch.
     * @param action The action performed on input value.
     * @return This object.
     */
    @ThreadSafe(false)
    public final Switch<I, O> defaultCase(final Function<? super I, ? extends O> action){
        this.defaultCase = action;
        return this;
    }

    @ThreadSafe(false)
    public final Switch<I, O> defaultCase(final O output) {
        return defaultCase(Functions.constant(output));
    }

    /**
     * Executes switch over conditions.
     * @param value The value to process.
     * @return Execution result.
     */
    @ThreadSafe
    @Override
    public final O apply(final I value){
        SwitchNode<I, O> lookup = first;
        while (lookup != null){
            if(lookup.predicate.apply(value))
                return lookup.transformer.apply(value);
            lookup = lookup.nextNode;
        }
        return defaultCase != null ? defaultCase.apply(value) : null;
    }

    @ThreadSafe
    public final <S extends O> S apply(final I value, final Class<S> resultType) {
        return resultType.cast(apply(value));
    }
}
