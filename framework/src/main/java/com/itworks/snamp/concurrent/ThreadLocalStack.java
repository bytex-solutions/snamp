package com.itworks.snamp.concurrent;

import com.itworks.snamp.SafeCloseable;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Represents a thread-local stack.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ThreadLocalStack<T> extends ThreadLocal<Deque<T>> implements SafeCloseable {
    private static final class ElementScope<T> implements SafeCloseable{
        private final Deque<T> stack;

        private ElementScope(final Deque<T> stack){
            this.stack = stack;
        }

        @Override
        public void close() throws NoSuchElementException{
            stack.pop();
        }
    }

    /**
     * Creates a new stack container isolated for the caller thread.
     * @return A new stack container.
     */
    @Override
    protected Deque<T> initialValue() {
        return new LinkedList<>();
    }

    /**
     * Pushes a new element into the thread-local stack.
     * @param item An item to push into the stack.
     */
    public final void push(final T item){
        get().push(item);
    }

    /**
     * Pushes a new element into the thread-local stack.
     * @param item An item to push into the stack.
     * @return Lexical scope controller that pops the pushed element.
     */
    public final SafeCloseable pushScope(final T item){
        push(item);
        return new ElementScope<>(get());
    }

    /**
     * Pops the element from the thread-local stack.
     * @return The element at the top of this stack.
     * @throws NoSuchElementException The stack is empty.
     */
    public final T pop() throws NoSuchElementException{
        return get().pop();
    }

    /**
     * Retrieves, but does not remove, the top element of this stack.
     * @return The top element of this stack.
     */
    public final T top() {
        return get().peek();
    }

    /**
     * Gets the actual size of this stack.
     * @return The actual size of this stack.
     */
    public final int size(){
        return get().size();
    }

    /**
     * Removes the current thread's value for this thread-local stack.
     * @see #remove()
     */
    @Override
    public final void close() {
        remove();
    }
}
