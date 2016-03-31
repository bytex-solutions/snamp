package com.bytex.snamp.jmx;

import com.google.common.base.Supplier;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Represents a builder of JMX Open Type.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface OpenTypeBuilder<T extends OpenType<?>> extends Supplier<T> {
    /**
     * Constructs a new JMX Open Type.
     * @return A new JMX Open Type.
     * @throws OpenDataException Unable to construct type.
     */
    T build() throws OpenDataException;

    /**
     * Constructs a new JMX Open Type.
     * @return A new JMX Open Type.
     * @throws java.lang.IllegalStateException Unable to construct type.
     */
    @Override
    T get() throws IllegalStateException;
}
