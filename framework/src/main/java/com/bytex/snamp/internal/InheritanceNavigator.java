package com.bytex.snamp.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents navigator through all inherited classes.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class InheritanceNavigator<C> implements Iterable<Class<? super C>> {
    private final Class<C> clazz;
    private final Class<? super C> stopClazz;

    private InheritanceNavigator(final Class<C> clazz, final Class<? super C> stopClazz){
        this.clazz = Objects.requireNonNull(clazz);
        this.stopClazz = Objects.requireNonNull(stopClazz);
    }

    /**
     * Gets class which inheritance path is reflected by this object.
     * @return Class definition.
     */
    public Class<C> getStartingClass(){
        return clazz;
    }

    /**
     * Constructs a new navigator through inheritance of the specified class.
     * @param clazz Class instance which inheritance path should be reflected.
     * @param <C> Class which inheritance path should be reflected.
     * @return Navigator through inheritance of the specified class.
     */
    public static <C> InheritanceNavigator<C> of(final Class<C> clazz){
        return of(clazz, Object.class);
    }

    /**
     * Constructs a new navigator through inheritance of the specified class.
     * @param clazz Class instance which inheritance path should be reflected.
     * @param stopClazz Some class in inheritance path used to stop navigation.
     * @param <C> Class which inheritance path should be reflected.
     * @return Navigator through inheritance of the specified class.
     */
    public static <C> InheritanceNavigator<C> of(final Class<C> clazz, final Class<? super C> stopClazz){
        return new InheritanceNavigator<>(clazz, stopClazz);
    }

    private static <C> boolean isValidFrame(final Class<? super C> current, final Class<? super C> stopClass){
        return !(current == null || current.equals(stopClass));
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz, stopClazz);
    }

    private boolean equals(final InheritanceNavigator<?> other){
        return clazz.equals(other.clazz) && stopClazz.equals(other.stopClazz);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof InheritanceNavigator<?> && equals((InheritanceNavigator<?>) other);
    }

    /**
     * Iterates through all classes in inheritance path.
     * @param action Consumer of classes.
     */
    @Override
    public void forEach(final Consumer<? super Class<? super C>> action) {
        for (Class<? super C> frame = clazz; isValidFrame(frame, stopClazz); frame = frame.getSuperclass())
            action.accept(frame);
    }

    private static final class InheritanceIterator<C> implements Iterator<Class<? super C>>{
        private Class<? super C> currentFrame;
        private final Class<? super C> stopClass;

        private InheritanceIterator(final Class<C> clazz, final Class<? super C> stopClazz){
            currentFrame = clazz;
            stopClass = stopClazz;
        }

        @Override
        public boolean hasNext() {
            return isValidFrame(currentFrame, stopClass);
        }

        @Override
        public Class<? super C> next() {
            if (hasNext()) {
                final Class<? super C> result = currentFrame;
                currentFrame = currentFrame.getSuperclass();
                return result;
            } else
                throw new NoSuchElementException();
        }
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Class<? super C>> iterator() {
        return new InheritanceIterator<>(clazz, stopClazz);
    }
}
