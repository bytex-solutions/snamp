package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.SafeCloseable;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.*;

/**
 * Represents decorator for {@link Lock}.
 * @version 2.0
 * @since 2.0
 */
public final class LockDecorator implements Serializable, Supplier<Lock> {
    private static final long serialVersionUID = -5122029652598077166L;

    //value object
    private final class LockScope implements SafeCloseable, Serializable {
        private static final long serialVersionUID = 3412766281454711201L;
        final Lock lock;

        private LockScope(final Lock lock){
            this.lock = Objects.requireNonNull(lock);
        }

        LockScope lock(){
            lock.lock();
            return this;
        }

        LockScope lock(final Duration timeout) throws InterruptedException, TimeoutException {
            if(timeout == null)
                lock.lockInterruptibly();
            else if(!lock.tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS))
                throw new TimeoutException(String.format("Lock cannot be acquired after '%s'", timeout));
            return this;
        }

        @Override
        public void close() {
            lock.unlock();
        }

        @Override
        public int hashCode() {
            return lock.hashCode();
        }

        private boolean equals(final LockScope other){
            return Objects.equals(lock, other.lock);
        }

        @Override
        public boolean equals(final Object other) {
            return other instanceof LockScope && equals((LockScope) other);
        }

        @Override
        public String toString() {
            return lock.toString();
        }
    }

    private final LockScope scope;

    /**
     * Decorates the lock.
     * @param lock The lock object to be decorated. Cannot be {@literal null}.
     */
    public LockDecorator(@Nonnull final Lock lock){
        this.scope = new LockScope(lock);
    }

    /**
     * Decorates read lock.
     * @param lock Read/write lock. Cannot be {@literal null}.
     * @return Decorator for read lock.
     */
    @Nonnull
    public static LockDecorator readLock(@Nonnull final ReadWriteLock lock){
        return new LockDecorator(lock.readLock());
    }

    /**
     * Decorates write lock.
     * @param lock Read/write lock. Cannot be {@literal null}.
     * @return Decorator for write lock.
     */
    @Nonnull
    public static LockDecorator writeLock(@Nonnull final ReadWriteLock lock){
        return new LockDecorator(lock.writeLock());
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public Lock get() {
        return scope.lock;
    }

    /**
     * Acquires a lock.
     * @return Lock scope.
     */
    public SafeCloseable acquireLock(){
        return scope.lock();
    }

    /**
     * Acquires a lock.
     * @param timeout Lock acquisition timeout. {@literal null} means infinite timeout.
     * @return Lock scope.
     * @throws InterruptedException The current thread is interrupted.
     * @throws TimeoutException The lock cannot be obtained in the specified timeout.
     */
    public SafeCloseable acquireLock(final Duration timeout) throws InterruptedException, TimeoutException{
        return scope.lock(timeout);
    }

    /**
     * Executes action inside of exclusive lock.
     *
     * @param action        Action to execute inside of exclusive lock. Cannot be {@literal null}.
     * @param <V>           Type of result produced by action.
     * @return Result produced by action.
     */
    public <V> V supply(final Supplier<? extends V> action) {
        try (final SafeCloseable ignored = acquireLock()) {
            return action.get();
        }
    }

    public <V> V supply(final Supplier<? extends V> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.get();
        }
    }

    public boolean supplyBool(final BooleanSupplier action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.getAsBoolean();
        }
    }

    public boolean supplyBool(final BooleanSupplier action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.getAsBoolean();
        }
    }

    public int supplyInt(final IntSupplier action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.getAsInt();
        }
    }

    public int supplyInt(final IntSupplier action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.getAsInt();
        }
    }

    public <I> double applyAsDouble(final I input, final ToDoubleFunction<? super I> action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.applyAsDouble(input);
        }
    }

    public <I> double applyAsDouble(final I input, final ToDoubleFunction<? super I> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try (final SafeCloseable ignored = acquireLock(timeout)) {
            return action.applyAsDouble(input);
        }
    }

    public <I, O> O apply(final I input, final Function<? super I, ? extends O> action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.apply(input);
        }
    }

    public <I, O> O apply(final I input, final Function<? super I, ? extends O> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.apply(input);
        }
    }

    public <I, E extends Throwable> void accept(final I input, final Acceptor<? super I, E> action) throws E {
        try(final SafeCloseable ignored = acquireLock()){
            action.accept(input);
        }
    }

    public <I, E extends Throwable> void accept(final I input, final Acceptor<? super I, E> action, final Duration timeout) throws E, TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.accept(input);
        }
    }

    public <I1, I2> void accept(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action){
        try(final SafeCloseable ignored = acquireLock()){
            action.accept(input1, input2);
        }
    }

    public <I1, I2> void accept(final I1 input1, final I2 input2, final BiConsumer<? super I1, ? super I2> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.accept(input1, input2);
        }
    }

    public <I1, I2, O> O apply(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action) {
        try(final SafeCloseable ignored = acquireLock()){
            return action.apply(input1, input2);
        }
    }

    public <I1, I2, O> O apply(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends O> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.apply(input1, input2);
        }
    }

    public void run(final Runnable action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.run();
        }
    }

    public void run(final Runnable action){
        try(final SafeCloseable ignored = acquireLock()){
            action.run();
        }
    }

    public Runnable runnable(final Runnable action) {
        return () -> run(action);
    }

    public <V> Callable<? extends V> callable(final Callable<? extends V> action) {
        return () -> call(action);
    }

    public <V> Callable<? extends V> callable(final Callable<? extends V> action, final Duration timeout) {
        return () -> call(action, timeout);
    }

    public <V> V call(final Callable<? extends V> action) throws Exception {
        try(final SafeCloseable ignored = acquireLock()){
            return action.call();
        }
    }

    public <V> V call(final Callable<? extends V> action, final Duration timeout) throws Exception {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            return action.call();
        }
    }

    public <I> void acceptLong(final I input1, final long input2, final ObjLongConsumer<? super I> action){
        try(final SafeCloseable ignored = acquireLock()){
            action.accept(input1, input2);
        }
    }

    public <I> void acceptLong(final I input1, final long input2, final ObjLongConsumer<? super I> action, final Duration timeout) throws TimeoutException, InterruptedException {
        try(final SafeCloseable ignored = acquireLock(timeout)){
            action.accept(input1, input2);
        }
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    private boolean equals(final LockDecorator other){
        return Objects.equals(get(), other.get());
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof LockDecorator && equals((LockDecorator) other);
    }
    
    @Override
    public String toString() {
        return get().toString();
    }
}
