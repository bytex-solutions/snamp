package com.bytex.snamp.concurrent;

import com.bytex.snamp.Acceptor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.1
 */
abstract class AbstractLazyReference<V> implements LazyReference<V>, Consumer<V>, Externalizable {
    private static final long serialVersionUID = -1523800313246914539L;

    abstract V getRawValue();

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(getRawValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        accept((V) in.readObject());
    }

    /**
     * Gets value stored in this container.
     *
     * @return A value stored in this container.
     */
    @Override
    public Optional<V> get(){
        return Optional.ofNullable(getRawValue());
    }

    @Override
    public final V get(final Supplier<? extends V> initializer){
        V result = getRawValue();
        if(result == null)
            synchronized (this){
                result = getRawValue();
                if(result == null)
                    accept(result = initializer.get());
            }
        return result;
    }

    @Override
    public final <I> V get(final I input, final Function<? super I, ? extends V> initializer){
        V result = getRawValue();
        if(result == null)
            synchronized (this){
                result = getRawValue();
                if(result == null)
                    accept(result = initializer.apply(input));
            }
        return result;
    }

    @Override
    public final <I1, I2> V get(final I1 input1, final I2 input2, final BiFunction<? super I1, ? super I2, ? extends V> initializer){
        V result = getRawValue();
        if(result == null)
            synchronized (this){
                result = getRawValue();
                if(result == null)
                    accept(result = initializer.apply(input1, input2));
            }
        return result;
    }

    @Override
    public final <E extends Throwable> V get(final Acceptor<? super Consumer<V>, E> initializer) throws E {
        V result = getRawValue();
        if (result == null)
            synchronized (this) {
                result = getRawValue();
                if (result == null) {
                    initializer.accept(this);
                    result = getRawValue();
                }
            }
        return result;
    }
}
