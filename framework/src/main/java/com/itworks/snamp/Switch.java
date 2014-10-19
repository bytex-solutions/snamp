package com.itworks.snamp;

import com.itworks.snamp.internal.annotations.ThreadSafe;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.Transformer;

import java.util.Objects;

/**
 * Represents alternative {@code switch} construction that uses any input type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe(false)
public final class Switch<I, O> {
    private static final class SwitchNode<I, O> {
        private final Predicate<I> predicate;
        private final Transformer<I, O> transformer;
        private SwitchNode<I, O> nextNode = null;

        private SwitchNode(final Predicate<I> condition,
                           final Transformer<I, O> action) {
            this.predicate = Objects.requireNonNull(condition, "condition is null.");
            this.transformer = Objects.requireNonNull(action, "action is null.");
        }

        private SwitchNode<I, O> append(final Predicate<I> condition,
                                        final Transformer<I, O> action) {
            return this.nextNode = new SwitchNode<>(condition, action);
        }
    }

    private SwitchNode<I, O> first;
    private SwitchNode<I, O> last;
    private Transformer<I, O> defaultCase;

    private Switch(){
        first = last = null;
        defaultCase = null;
    }

    /**
     * Initializes a new switch.
     * @param <I> The input value to compare.
     * @param <O> The result of the switch operation.
     * @return A new switch controller.
     */
    public static <I, O> Switch<I, O> init() {
        return new Switch<>();
    }

    /**
     * Adds a new case for this switch.
     * @param condition The condition evaluator.
     * @param action The action performed on input value.
     * @return This object.
     */
    public Switch<I, O> addCase(final Predicate<I> condition,
                        final Transformer<I, O> action){
        if(first == null)
            first = last = new SwitchNode<>(condition, action);
        else last = last.append(condition, action);
        return this;
    }

    public Switch<I, O> equals(final I value,
                               final Transformer<I, O> action) {
        return addCase(new Predicate<I>() {
            @Override
            public boolean evaluate(final I other) {
                return Objects.equals(value, other);
            }
        }, action);
    }

    public Switch<I, O> equals(final I value,
                               final O output) {
        return equals(value, new Transformer<I, O>() {
            @Override
            public O transform(final I input) {
                return output;
            }
        });
    }

    public Switch<I, O> theSame(final I value,
                                final Transformer<I, O> action){
        return addCase(new Predicate<I>() {
            @Override
            public boolean evaluate(final I other) {
                return System.identityHashCode(value) == System.identityHashCode(other);
            }
        }, action);
    }

    public Switch<I, O> theSame(final I value,
                                final O output) {
        return theSame(value, new Transformer<I, O>() {
            @Override
            public O transform(final I input) {
                return output;
            }
        });
    }

    /**
     * Assigns default case to this switch.
     * @param action The action performed on input value.
     * @return This object.
     */
    public Switch<I, O> defaultCase(final Transformer<I, O> action){
        this.defaultCase = action;
        return this;
    }

    public Switch<I, O> defaultCase(final O output) {
        return defaultCase(new Transformer<I, O>() {
            @Override
            public O transform(final I input) {
                return output;
            }
        });
    }

    /**
     * Executes switch over conditions.
     * @param value The value to process.
     * @return Execution result.
     */
    public O execute(final I value){
        SwitchNode<I, O> lookup = first;
        while (lookup != null){
            if(lookup.predicate.evaluate(value))
                return lookup.transformer.transform(value);
            lookup = lookup.nextNode;
        }
        return defaultCase != null ? defaultCase.transform(value) : null;
    }

    public <S extends O> S execute(final I value, final Class<S> resultType) {
        return resultType.cast(execute(value));
    }
}
