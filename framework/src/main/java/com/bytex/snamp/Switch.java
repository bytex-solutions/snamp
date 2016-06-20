package com.bytex.snamp;

import com.google.common.base.*;

import java.util.Objects;
import java.util.concurrent.*;

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
    protected static abstract class CaseStatement<I, O> implements Function<I, O> {
        private CaseStatement<I, O> nextNode;

        /**
         * Determines whether transformation can be applied to the specified value.
         * @param value The value to check.
         * @return {@literal true}, if transformation can be applied to the specified value; otherwise, {@literal false}.
         */
        public abstract boolean match(final I value);

        private CaseStatement<I, O> append(final CaseStatement<I, O> nextCase) {
            return this.nextNode = nextCase;
        }

        private Callable<O> createTask(final I value) {
            return () -> match(value) ? apply(value) : null;
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
    }

    /**
     * Represents case-statement with forwarding of condition check and transformation
     * to the specified instances of functional interfaces.
     * @param <I> Input value to check.
     * @param <O> Transformation result.
     */
    protected static final class ForwardingStatement<I, O> extends CaseStatement<I, O>{
        private final Predicate<? super I> matcher;
        private final Function<? super I, ? extends O> transformer;

        public ForwardingStatement(final Predicate<? super I> matcher,
                                 final Function<? super I, ? extends O> transformer){
            this.matcher = Objects.requireNonNull(matcher, "matcher is null");
            this.transformer = Objects.requireNonNull(transformer, "transformer is null");
        }

        @Override
        public boolean match(final I value) {
            return matcher.apply(value);
        }

        @Override
        public O apply(final I input) {
            return transformer.apply(input);
        }
    }

    private CaseStatement<I, O> first;
    private CaseStatement<I, O> last;
    private Function<? super I, ? extends O> defaultCase;

    private static <I, O> CaseStatement<I, O> equalsToNullStatement(final Supplier<? extends O> action){
        return new CaseStatement<I, O>() {
            @Override
            public boolean match(final I value) {
                return value == null;
            }

            @Override
            public O apply(final Object input) {
                return action.get();
            }
        };
    }

    private static <I, O> CaseStatement<I, O> equalsStatement(final I expected,
                                                              final Function<? super I, ? extends O> action){
        return new CaseStatement<I, O>() {
            @Override
            public boolean match(final I value) {
                return Objects.equals(expected, value);
            }

            @Override
            public O apply(final I input) {
                return action.apply(input);
            }
        };
    }

    private static <I, O> CaseStatement<I, O> identityStatement(final I expected,
                                                                  final Function<? super I, ? extends O> action){
        return new CaseStatement<I, O>() {
            @Override
            public boolean match(final I value) {
                return value == expected;
            }

            @Override
            public O apply(final I input) {
                return action.apply(input);
            }
        };
    }

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
                        final Function<? super I, ? extends O> action){
        return addCase(new ForwardingStatement<>(condition, action));
    }

    public final <T> Switch<I, O> instanceOf(final Class<T> type, final Function<? super T, ? extends O> action) {
        return addCase(Predicates.instanceOf(type), Functions.compose(action, CastFunction.of(type)));
    }

    public final Switch<I, O> instanceOf(final Class<?> type, final O value) {
        return instanceOf(type, Functions.constant(value));
    }

    @ThreadSafe(false)
    public final Switch<I, O> equals(final I value,
                               final Function<? super I, ? extends O> action) {
        return addCase(equalsStatement(value, action));
    }

    @ThreadSafe(false)
    public final Switch<I, O> equals(final I expected,
                                     final O output) {
        return equals(expected, Functions.constant(output));
    }

    @ThreadSafe(false)
    public final Switch<I, O> equalsToNull(final Supplier<? extends O> action){
        return addCase(Switch.<I, O>equalsToNullStatement(action));
    }

    @ThreadSafe(false)
    public final Switch<I, O> equalsToNull(final O result){
        return equalsToNull(Suppliers.ofInstance(result));
    }

    @ThreadSafe(false)
    public final Switch<I, O> theSame(final I value,
                                final Function<? super I, ? extends O> action){
        return addCase(identityStatement(value, action));
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
    public final Switch<I, O> otherwise(final Function<? super I, ? extends O> action){
        this.defaultCase = action;
        return this;
    }

    @ThreadSafe(false)
    public final Switch<I, O> otherwise(final O output) {
        return otherwise(Functions.constant(output));
    }

    public final O apply(final I value, final Function<? super I, ? extends O> defaultCase){
        for (CaseStatement<I, O> lookup = first; lookup != null; lookup = lookup.getNextNode())
            if (lookup.match(value))
                return lookup.apply(value);
        return defaultCase != null ? defaultCase.apply(value) : null;
    }

    public final O apply(final I value, final O defaultResult){
        return apply(value, Functions.constant(defaultResult));
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
        return apply(value, new ExecutorCompletionService<O>(executor));
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
