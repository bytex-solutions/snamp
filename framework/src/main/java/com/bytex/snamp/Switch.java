package com.bytex.snamp;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import java.util.Objects;
import java.util.concurrent.*;

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

        private Callable<O> createTask(final I value) {
            return new ExceptionalCallable<O, ExceptionPlaceholder>() {
                @Override
                public O call() {
                    return predicate.apply(value) ? transformer.apply(value) : null;
                }
            };
        }
    }

    private SwitchNode<I, O> first = null;
    private SwitchNode<I, O> last = null;
    private Function<? super I, ? extends O> defaultCase = null;

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

    private static <I> Predicate<I> identityEquals(final I value){
        return new Predicate<I>() {
            @Override
            public boolean apply(final I other) {
                return System.identityHashCode(value) == System.identityHashCode(other);
            }
        };
    }

    @ThreadSafe(false)
    public final Switch<I, O> theSame(final I value,
                                final Function<? super I, ? extends O> action){
        return addCase(identityEquals(value), action);
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
    public final O apply(final I value) {
        for (SwitchNode<I, O> lookup = first; lookup != null; lookup = lookup.nextNode)
            if (lookup.predicate.apply(value))
                return lookup.transformer.apply(value);
        return defaultCase != null ? defaultCase.apply(value) : null;
    }

    private O apply(final I value, final CompletionService<O> batch) throws InterruptedException, ExecutionException {
        int submitted = 0;
        for (SwitchNode<I, O> lookup = first; lookup != null; lookup = lookup.nextNode, submitted++)
            batch.submit(lookup.createTask(value));
        for(;submitted >0; submitted--){
            final O result = batch.take().get();
            if(result != null) return result;
        }
        return defaultCase != null ? defaultCase.apply(value) : null;
    }

    /**
     * Executes switch over conditions in parallel manner.
     * @param value The value to process.
     * @param executor Executor used to fork executions.
     * @return Execution result.
     * @throws ExecutionException Some condition raises exception.
     * @throws InterruptedException Caller thread interrupted before execution was completed.
     */
    public final O apply(final I value, final Executor executor) throws ExecutionException, InterruptedException {
        return apply(value, new ExecutorCompletionService<O>(executor));
    }

    /**
     * Removes all statements in this object.
     * @return This object.
     */
    public final Switch<I, O> reset(){
        first = last = null;
        defaultCase = null;
        return this;
    }
}
