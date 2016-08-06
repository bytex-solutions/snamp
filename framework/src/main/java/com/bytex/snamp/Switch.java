package com.bytex.snamp;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents alternative {@code switch} construction that uses any input type.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class Switch<I, O> implements Function<I, O> {
    /**
     * Represents combination of condition check and transformation action.
     * @param <I> Type of the input value.
     * @param <O> Type of the transformation result.
     */
    protected static abstract class CaseStatement<I, O> implements Function<I, O>, Predicate<I> {
        private CaseStatement<I, O> nextNode;

        private CaseStatement<I, O> append(final CaseStatement<I, O> nextCase) {
            return this.nextNode = nextCase;
        }

        private Callable<O> createTask(final I value) {
            return () -> test(value) ? apply(value) : null;
        }

        /**
         * Releases linked nodes.
         */
        private void clear(){
            if(nextNode != null)
                nextNode.clear();
            nextNode = null;
        }

        private CaseStatement<I, O> getNextNode(){
            return nextNode;
        }

        public static <I, O> CaseStatement<I, O> create(final Predicate<? super I> condition,
                                                        final Function<? super I, ? extends O> action){
            return new CaseStatement<I, O>() {
                @Override
                public boolean test(final I value) {
                    return condition.test(value);
                }

                @Override
                public O apply(final I value) {
                    return action.apply(value);
                }
            };
        }

        public static <I, O> CaseStatement<I, O> create(final Predicate<? super I> condition,
                                                        final Supplier<? extends O> action){
            return new CaseStatement<I, O>() {
                @Override
                public boolean test(final I value) {
                    return condition.test(value);
                }

                @Override
                public O apply(final I value) {
                    return action.get();
                }
            };
        }
    }

    private CaseStatement<I, O> first;
    private CaseStatement<I, O> last;
    private Function<? super I, ? extends O> defaultCase;

    /**
     * Adds a new case for this switch.
     * @param stmt The case statement to append.
     * @return A reference to the modified switch.
     */
    protected final Switch<I, O> addCase(final CaseStatement<I, O> stmt){
        if(first == null) first = last = stmt;
        else last = last.append(stmt);
        return this;
    }

    /**
     * Adds a new case for this switch.
     * @param condition The condition evaluator.
     * @param action The action performed on input value.
     * @return This object.
     */
    @ThreadSafe(false)
    public final Switch<I, O> addCase(final Predicate<? super I> condition,
                        final Function<? super I, ? extends O> action) {
        return addCase(CaseStatement.create(condition, action));
    }

    /**
     * Adds a new case for this switch.
     * @param condition The condition evaluator.
     * @param action The action performed on input value.
     * @return This object.
     */
    @ThreadSafe(false)
    public final Switch<I, O> addCase(final Predicate<? super I> condition,
                                      final Supplier<? extends O> action) {
        return addCase(CaseStatement.create(condition, action));
    }

    public final <T> Switch<I, O> instanceOf(final Class<T> type, final Function<? super T, ? extends O> action) {
        return addCase(type::isInstance, ((Function<? super I, ? extends T>) type::cast).andThen(action));
    }

    public final Switch<I, O> instanceOf(final Class<?> type, final O value) {
        return instanceOf(type, obj -> value);
    }

    @ThreadSafe(false)
    public final Switch<I, O> equals(final I value,
                               final Function<? super I, ? extends O> action) {
        return addCase(CaseStatement.create(other -> Objects.equals(value, other), action));
    }

    @ThreadSafe(false)
    public final Switch<I, O> equals(final I expected,
                                     final O output) {
        return equals(expected, obj -> output);
    }

    @ThreadSafe(false)
    public final Switch<I, O> equalsToNull(final Supplier<? extends O> action){
        return addCase(CaseStatement.create(value -> value == null, nullVal -> action.get()));
    }

    @ThreadSafe(false)
    public final Switch<I, O> equalsToNull(final O result){
        return equalsToNull(() -> result);
    }

    @ThreadSafe(false)
    public final Switch<I, O> theSame(final I value,
                                final Function<? super I, ? extends O> action){
        return addCase(CaseStatement.create(other -> value == other, action));
    }

    @ThreadSafe(false)
    public final Switch<I, O> theSame(final I value,
                                final O output) {
        return theSame(value, obj -> output);
    }

    /**
     * Assigns default case to this switch.
     * @param action The action performed on input value.
     * @return This object.
     */
    @ThreadSafe(false)
    public final Switch<I, O> otherwise(final Function<? super I, ? extends O> action){
        this.defaultCase = action;
        return this;
    }

    @ThreadSafe(false)
    public final Switch<I, O> otherwise(final O output) {
        return otherwise(obj -> output);
    }

    public final O apply(final I value, final Function<? super I, ? extends O> defaultCase){
        for (CaseStatement<I, O> lookup = first; lookup != null; lookup = lookup.getNextNode())
            if (lookup.test(value))
                return lookup.apply(value);
        return defaultCase != null ? defaultCase.apply(value) : null;
    }

    public final O apply(final I value, final O defaultResult) {
        return apply(value, obj -> defaultResult);
    }

    /**
     * Executes switch over conditions.
     * @param value The value to process.
     * @return Execution result.
     */
    @ThreadSafe
    @Override
    public final O apply(final I value) {
        return apply(value, defaultCase);
    }

    private O apply(final I value, final CompletionService<O> batch) throws InterruptedException, ExecutionException {
        int submitted = 0;
        for (CaseStatement<I, O> lookup = first; lookup != null; lookup = lookup.getNextNode(), submitted++)
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
        return apply(value, new ExecutorCompletionService<>(executor));
    }

    /**
     * Removes all statements in this object.
     * @return This object.
     */
    public final Switch<I, O> reset() {
        if (first != null)
            first.clear();
        first = last = null;
        defaultCase = null;
        return this;
    }
}
