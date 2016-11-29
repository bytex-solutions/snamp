package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;

/**
 * Represents a builder of JMX Open Type.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface OpenTypeBuilder<T extends OpenType<?>> {
    /**
     * Constructs a new JMX Open Type.
     * @return A new JMX Open Type.
     * @throws OpenDataException Unable to construct type.
     */
    T build() throws OpenDataException;
}
