package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Represents a builder of JMX Open Type.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface OpenTypeBuilder<T extends OpenType<?>> extends Supplier<T>, Callable<T> {
    /**
     * Constructs a new JMX Open Type.
     * @return A new JMX Open Type.
     * @throws OpenDataException Unable to construct type.
     */
    T call() throws OpenDataException;

    /**
     * Constructs a new JMX Open Type.
     * @return A new JMX Open Type.
     * @throws java.lang.IllegalStateException Unable to construct type.
     */
    @Override
    T get() throws IllegalStateException;
}
