package com.itworks.snamp.internal;

import org.apache.commons.collections4.Closure;
import org.apache.commons.collections4.FunctorException;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * Represents a bridge between transformer implementation and {@link org.apache.commons.collections4.Closure} interface.
 * <p>
 *     Usually, this class can be used to obtain return value from the {@link org.apache.commons.collections4.Closure}
 *     functional interface invocation.
 * </p>
 * @param <I> Type of the value to transform.
 * @param <O> The transformation value.
 */
public abstract class TransformerClosure<I, O> extends MutableObject<O> implements Closure<I>, Transformer<I, O> {

    /**
     * Performs an action on the specified input object.
     *
     * @param input the input to execute on
     * @throws org.apache.commons.collections4.FunctorException         (runtime) if any other error occurs
     */
    @Override
    public final void execute(final I input) throws FunctorException {
        setValue(transform(input));
    }
}
