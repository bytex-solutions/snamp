package com.bytex.snamp.concurrent;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents standalone future which computation can be executed in the separated thread.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@ThreadSafe
public class FutureThread<V> extends Thread implements ComputationPipeline<V>{

    private final Callable<V> implementation;
    private final CompletableFuture<V> result;

    /**
     * Initializes a new standalone future.
     * @param impl Implementation of the thread. Cannot be {@literal null}.
     */
    public FutureThread(final Callable<V> impl){
        this(null, impl);
    }

    /**
     * Initializes a new standalone future.
     * @param group Thread group for this future.
     * @param impl Implementation of the thread. Cannot be {@literal null}.
     */
    public FutureThread(final ThreadGroup group, final Callable<V> impl){
        this(group, impl, impl.toString());
    }

    /**
     * Initializes a new standalone future.
     * @param group Thread group for this future.
     * @param impl Implementation of the thread. Cannot be {@literal null}.
     * @param name The name of the thread.
     * @since 1.2
     */
    public FutureThread(final ThreadGroup group, final Callable<V> impl, final String name){
        super(group, name);
        implementation = impl;
        setDaemon(true);
        result = new CompletableFuture<>();
    }

    /**
     * Executes a new task in the separated thread.
     * @param task The task to apply in the separated thread.
     * @param <V> Type of the asynchronous computation result.
     * @return An object that represents the state of asynchronous computation.
     */
    public static <V> FutureThread<V> start(final Callable<V> task) {
        final FutureThread<V> future = new FutureThread<>(task);
        future.start();
        return future;
    }

    /**
     * Executes a new task in the separated thread.
     * @param task The task to apply in the separated thread.
     * @return An object that represents the state of asynchronous computation.
     * @since 1.2
     */
    public static FutureThread<Void> start(final Runnable task) {
        return start(() -> {
            task.run();
            return null;
        });
    }

    /**
     * Executes this thread.
     */
    @Override
    public final void run() {
        try {
            result.complete(implementation.call());
        } catch (final InterruptedException e) {
            result.cancel(true);
        } catch (final Throwable e) {
            result.completeExceptionally(e);
        }
    }

    /**
     * Attempts to cancel execution of this task.  This attempt will
     * fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason. If successful,
     * and this task has not started when <tt>cancel</tt> is called,
     * this task should never run.  If the task has already started,
     * then the <tt>mayInterruptIfRunning</tt> parameter determines
     * whether the thread executing this task should be interrupted in
     * an attempt to stop the task.
     * <p/>
     * <p>After this method returns, subsequent calls to {@link #isDone} will
     * always return <tt>true</tt>.  Subsequent calls to {@link #isCancelled}
     * will always return <tt>true</tt> if this method returned <tt>true</tt>.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return <tt>false</tt> if the task could not be cancelled,
     *         typically because it has already completed normally;
     *         <tt>true</tt> otherwise
     */
    @Override
    public final boolean cancel(final boolean mayInterruptIfRunning) {
        if (isInterrupted()) return false;
        switch (getState()) {
            case NEW:
                return result.cancel(mayInterruptIfRunning);
            case TERMINATED:
                return false;
            case BLOCKED:
            case WAITING:
            case TIMED_WAITING:
            case RUNNABLE:
                if (mayInterruptIfRunning) {
                    interrupt();
                    return result.cancel(true);
                } else return false;
            default:
                return false;
        }
    }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed
     * normally.
     *
     * @return <tt>true</tt> if this task was cancelled before it completed
     */
    @Override
    public final boolean isCancelled() {
        return result.isCancelled();
    }

    /**
     * Returns <tt>true</tt> if this task completed.
     * <p/>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * <tt>true</tt>.
     *
     * @return <tt>true</tt> if this task completed
     */
    @Override
    public final boolean isDone() {
        return result.isDone();
    }

    /**
     * Waits if necessary for the computation to complete, and then
     * retrieves its result.
     *
     * @return the computed result
     * @throws java.util.concurrent.CancellationException
     *                              if the computation was cancelled
     * @throws java.util.concurrent.ExecutionException
     *                              if the computation threw an
     *                              exception
     * @throws InterruptedException if the current thread was interrupted
     *                              while waiting
     */
    @Override
    public final V get() throws InterruptedException, ExecutionException {
        return result.get();
    }

    /**
     * Waits if necessary for at most the given time for the computation
     * to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return the computed result
     * @throws java.util.concurrent.CancellationException
     *                              if the computation was cancelled
     * @throws java.util.concurrent.ExecutionException
     *                              if the computation threw an
     *                              exception
     * @throws InterruptedException if the current thread was interrupted
     *                              while waiting
     * @throws java.util.concurrent.TimeoutException
     *                              if the wait timed out
     */
    @Override
    public final V get(final long timeout, @Nonnull final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return result.get(timeout, unit);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed with this stage's result as the argument
     * to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function to use to compute the value of
     *           the returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> thenApply(final Function<? super V, ? extends U> fn) {
        return result.thenApply(fn);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using this stage's default asynchronous
     * execution facility, with this stage's result as the argument to
     * the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function to use to compute the value of
     *           the returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> thenApplyAsync(final Function<? super V, ? extends U> fn) {
        return result.thenApplyAsync(fn);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using the supplied Executor, with this
     * stage's result as the argument to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn       the function to use to compute the value of
     *                 the returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> thenApplyAsync(final Function<? super V, ? extends U> fn, final Executor executor) {
        return result.thenApplyAsync(fn, executor);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed with this stage's result as the argument
     * to the supplied action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> thenAccept(final Consumer<? super V> action) {
        return result.thenAccept(action);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using this stage's default asynchronous
     * execution facility, with this stage's result as the argument to
     * the supplied action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> thenAcceptAsync(final Consumer<? super V> action) {
        return result.thenAcceptAsync(action);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using the supplied Executor, with this
     * stage's result as the argument to the supplied action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param action   the action to perform before completing the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> thenAcceptAsync(final Consumer<? super V> action, final Executor executor) {
        return result.thenAcceptAsync(action, executor);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, executes the given action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> thenRun(final Runnable action) {
        return result.thenRun(action);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, executes the given action using this stage's default
     * asynchronous execution facility.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> thenRunAsync(final Runnable action) {
        return result.thenRunAsync(action);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, executes the given action using the supplied Executor.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param action   the action to perform before completing the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> thenRunAsync(final Runnable action, final Executor executor) {
        return result.thenRunAsync(action, executor);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage both complete normally, is executed with the two
     * results as arguments to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param fn    the function to use to compute the value of
     *              the returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U, V1> CompletionStage<V1> thenCombine(final CompletionStage<? extends U> other, final BiFunction<? super V, ? super U, ? extends V1> fn) {
        return result.thenCombine(other, fn);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using this stage's
     * default asynchronous execution facility, with the two results
     * as arguments to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param fn    the function to use to compute the value of
     *              the returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U, V1> CompletionStage<V1> thenCombineAsync(final CompletionStage<? extends U> other, final BiFunction<? super V, ? super U, ? extends V1> fn) {
        return result.thenCombineAsync(other, fn);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using the supplied
     * executor, with the two results as arguments to the supplied
     * function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other    the other CompletionStage
     * @param fn       the function to use to compute the value of
     *                 the returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final <U, V1> CompletionStage<V1> thenCombineAsync(final CompletionStage<? extends U> other, final BiFunction<? super V, ? super U, ? extends V1> fn, final Executor executor) {
        return result.thenCombineAsync(other, fn, executor);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage both complete normally, is executed with the two
     * results as arguments to the supplied action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<Void> thenAcceptBoth(final CompletionStage<? extends U> other, final BiConsumer<? super V, ? super U> action) {
        return result.thenAcceptBoth(other, action);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using this stage's
     * default asynchronous execution facility, with the two results
     * as arguments to the supplied action.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other, final BiConsumer<? super V, ? super U> action) {
        return result.thenAcceptBothAsync(other, action);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, is executed using the supplied
     * executor, with the two results as arguments to the supplied
     * function.
     *
     * @param other    the other CompletionStage
     * @param action   the action to perform before completing the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<Void> thenAcceptBothAsync(final CompletionStage<? extends U> other, final BiConsumer<? super V, ? super U> action, final Executor executor) {
        return result.thenAcceptBothAsync(other, action, executor);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage both complete normally, executes the given action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> runAfterBoth(final CompletionStage<?> other, final Runnable action) {
        return result.runAfterBoth(other, action);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, executes the given action using
     * this stage's default asynchronous execution facility.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action) {
        return result.runAfterBothAsync(other, action);
    }

    /**
     * Returns a new CompletionStage that, when this and the other
     * given stage complete normally, executes the given action using
     * the supplied executor.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other    the other CompletionStage
     * @param action   the action to perform before completing the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> runAfterBothAsync(final CompletionStage<?> other, final Runnable action, final Executor executor) {
        return result.runAfterBothAsync(other, action, executor);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed with the
     * corresponding result as argument to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param fn    the function to use to compute the value of
     *              the returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> applyToEither(final CompletionStage<? extends V> other, final Function<? super V, U> fn) {
        return result.applyToEither(other, fn);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using this
     * stage's default asynchronous execution facility, with the
     * corresponding result as argument to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other the other CompletionStage
     * @param fn    the function to use to compute the value of
     *              the returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> applyToEitherAsync(final CompletionStage<? extends V> other, final Function<? super V, U> fn) {
        return result.applyToEitherAsync(other, fn);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using the
     * supplied executor, with the corresponding result as argument to
     * the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other    the other CompletionStage
     * @param fn       the function to use to compute the value of
     *                 the returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> applyToEitherAsync(final CompletionStage<? extends V> other, final Function<? super V, U> fn, final Executor executor) {
        return result.applyToEitherAsync(other, fn, executor);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed with the
     * corresponding result as argument to the supplied action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> acceptEither(final CompletionStage<? extends V> other, final Consumer<? super V> action) {
        return result.acceptEither(other, action);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using this
     * stage's default asynchronous execution facility, with the
     * corresponding result as argument to the supplied action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> acceptEitherAsync(final CompletionStage<? extends V> other, final Consumer<? super V> action) {
        return result.acceptEitherAsync(other, action);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, is executed using the
     * supplied executor, with the corresponding result as argument to
     * the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other    the other CompletionStage
     * @param action   the action to perform before completing the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> acceptEitherAsync(final CompletionStage<? extends V> other, final Consumer<? super V> action, final Executor executor) {
        return result.acceptEitherAsync(other, action, executor);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, executes the given action.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> runAfterEither(final CompletionStage<?> other, final Runnable action) {
        return result.runAfterEither(other, action);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, executes the given action
     * using this stage's default asynchronous execution facility.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other  the other CompletionStage
     * @param action the action to perform before completing the
     *               returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action) {
        return result.runAfterEitherAsync(other, action);
    }

    /**
     * Returns a new CompletionStage that, when either this or the
     * other given stage complete normally, executes the given action
     * using the supplied executor.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param other    the other CompletionStage
     * @param action   the action to perform before completing the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<Void> runAfterEitherAsync(final CompletionStage<?> other, final Runnable action, final Executor executor) {
        return result.runAfterEitherAsync(other, action, executor);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed with this stage as the argument
     * to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function returning a new CompletionStage
     * @return the CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> thenCompose(final Function<? super V, ? extends CompletionStage<U>> fn) {
        return result.thenCompose(fn);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using this stage's default asynchronous
     * execution facility, with this stage as the argument to the
     * supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn the function returning a new CompletionStage
     * @return the CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> thenComposeAsync(final Function<? super V, ? extends CompletionStage<U>> fn) {
        return result.thenComposeAsync(fn);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * normally, is executed using the supplied Executor, with this
     * stage's result as the argument to the supplied function.
     * <p>
     * See the {@link CompletionStage} documentation for rules
     * covering exceptional completion.
     *
     * @param fn       the function returning a new CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> thenComposeAsync(final Function<? super V, ? extends CompletionStage<U>> fn, final Executor executor) {
        return result.thenComposeAsync(fn, executor);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * exceptionally, is executed with this stage's exception as the
     * argument to the supplied function.  Otherwise, if this stage
     * completes normally, then the returned stage also completes
     * normally with the same value.
     *
     * @param fn the function to use to compute the value of the
     *           returned CompletionStage if this CompletionStage completed
     *           exceptionally
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<V> exceptionally(final Function<Throwable, ? extends V> fn) {
        return result.exceptionally(fn);
    }

    /**
     * Returns a new CompletionStage with the same result or exception as
     * this stage, that executes the given action when this stage completes.
     * <p>
     * <p>When this stage is complete, the given action is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null}
     * if none) of this stage as arguments.  The returned stage is completed
     * when the action returns.  If the supplied action itself encounters an
     * exception, then the returned stage exceptionally completes with this
     * exception unless this stage also completed exceptionally.
     *
     * @param action the action to perform
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<V> whenComplete(final BiConsumer<? super V, ? super Throwable> action) {
        return result.whenComplete(action);
    }

    /**
     * Returns a new CompletionStage with the same result or exception as
     * this stage, that executes the given action using this stage's
     * default asynchronous execution facility when this stage completes.
     * <p>
     * <p>When this stage is complete, the given action is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null}
     * if none) of this stage as arguments.  The returned stage is completed
     * when the action returns.  If the supplied action itself encounters an
     * exception, then the returned stage exceptionally completes with this
     * exception unless this stage also completed exceptionally.
     *
     * @param action the action to perform
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<V> whenCompleteAsync(final BiConsumer<? super V, ? super Throwable> action) {
        return result.whenCompleteAsync(action);
    }

    /**
     * Returns a new CompletionStage with the same result or exception as
     * this stage, that executes the given action using the supplied
     * Executor when this stage completes.
     * <p>
     * <p>When this stage is complete, the given action is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null}
     * if none) of this stage as arguments.  The returned stage is completed
     * when the action returns.  If the supplied action itself encounters an
     * exception, then the returned stage exceptionally completes with this
     * exception unless this stage also completed exceptionally.
     *
     * @param action   the action to perform
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final CompletionStage<V> whenCompleteAsync(final BiConsumer<? super V, ? super Throwable> action, final Executor executor) {
        return result.whenCompleteAsync(action, executor);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * either normally or exceptionally, is executed with this stage's
     * result and exception as arguments to the supplied function.
     * <p>
     * <p>When this stage is complete, the given function is invoked
     * with the result (or {@code null} if none) and the exception (or
     * {@code null} if none) of this stage as arguments, and the
     * function's result is used to complete the returned stage.
     *
     * @param fn the function to use to compute the value of the
     *           returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> handle(final BiFunction<? super V, Throwable, ? extends U> fn) {
        return result.handle(fn);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * either normally or exceptionally, is executed using this stage's
     * default asynchronous execution facility, with this stage's
     * result and exception as arguments to the supplied function.
     * <p>
     * <p>When this stage is complete, the given function is invoked
     * with the result (or {@code null} if none) and the exception (or
     * {@code null} if none) of this stage as arguments, and the
     * function's result is used to complete the returned stage.
     *
     * @param fn the function to use to compute the value of the
     *           returned CompletionStage
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> handleAsync(final BiFunction<? super V, Throwable, ? extends U> fn) {
        return result.handleAsync(fn);
    }

    /**
     * Returns a new CompletionStage that, when this stage completes
     * either normally or exceptionally, is executed using the
     * supplied executor, with this stage's result and exception as
     * arguments to the supplied function.
     * <p>
     * <p>When this stage is complete, the given function is invoked
     * with the result (or {@code null} if none) and the exception (or
     * {@code null} if none) of this stage as arguments, and the
     * function's result is used to complete the returned stage.
     *
     * @param fn       the function to use to compute the value of the
     *                 returned CompletionStage
     * @param executor the executor to use for asynchronous execution
     * @return the new CompletionStage
     */
    @Override
    public final <U> CompletionStage<U> handleAsync(final BiFunction<? super V, Throwable, ? extends U> fn, final Executor executor) {
        return result.handleAsync(fn, executor);
    }

    /**
     * Returns a {@link CompletableFuture} maintaining the same
     * completion properties as this stage. If this stage is already a
     * CompletableFuture, this method may return this stage itself.
     * Otherwise, invocation of this method may be equivalent in
     * effect to {@code thenApply(x -> x)}, but returning an instance
     * of type {@code CompletableFuture}. A CompletionStage
     * implementation that does not choose to interoperate with others
     * may throw {@code UnsupportedOperationException}.
     *
     * @return the CompletableFuture
     * @throws UnsupportedOperationException if this implementation
     *                                       does not interoperate with CompletableFuture
     */
    @Override
    public final CompletableFuture<V> toCompletableFuture() {
        return result;
    }
}
