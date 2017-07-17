package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.Stateful;

import javax.annotation.Nonnull;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Predicate;

/**
 * Represents {@link HashSet} with implemented {@link Modifiable}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class ModifiableHashSet<E> extends HashSet<E> implements Modifiable, Externalizable, Stateful, SerializableSet<E> {
    private static final long serialVersionUID = -2889573187476879345L;
    private volatile boolean modified;

    ModifiableHashSet() {
        modified = false;
    }

    @Override
    public final void clear() {
        super.clear();
        markAsModified();
    }

    @Override
    public final boolean add(final E e) {
        final boolean added = super.add(e);
        modified |= added;
        return added;
    }

    @Override
    public final boolean remove(final Object o) {
        final boolean removed = super.remove(o);
        modified |= removed;
        return removed;
    }

    @Override
    public final boolean removeAll(@Nonnull final Collection<?> c) {
        final boolean removed = super.removeAll(c);
        modified |= removed;
        return removed;
    }

    @Override
    public final boolean addAll(@Nonnull final Collection<? extends E> c) {
        final boolean added = super.addAll(c);
        modified |= added;
        return added;
    }

    @Override
    public final boolean retainAll(@Nonnull final Collection<?> c) {
        final boolean retained = super.retainAll(c);
        modified |= retained;
        return retained;
    }

    @Override
    public final boolean removeIf(final Predicate<? super E> filter) {
        final boolean removed = super.removeIf(filter);
        modified |= removed;
        return removed;
    }

    final void markAsModified(){
        modified = true;
    }

    @Override
    public void reset() {
        modified = false;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    abstract void writeItem(final E item, final ObjectOutput out) throws IOException;

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public final void writeExternal(final ObjectOutput out) throws IOException {
        //save overridden properties
        out.writeInt(size());
        for(final E item: this)
            writeItem(item, out);
    }

    abstract E readItem(final ObjectInput in) throws IOException, ClassNotFoundException;

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @Override
    public final void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        //restore overridden properties
        for (int size = in.readInt(); size > 0; size--)
            add(readItem(in));
    }
}
