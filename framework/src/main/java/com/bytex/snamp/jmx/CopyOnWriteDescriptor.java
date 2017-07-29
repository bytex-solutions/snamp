package com.bytex.snamp.jmx;

import javax.management.Descriptor;
import java.util.Map;

/**
 * Represents immutable descriptor that can be modified
 * through cloning.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public interface CopyOnWriteDescriptor extends Descriptor {
    /**
     * Returns cloned descriptor with modified fields.
     * @param values A fields to put into the new descriptor.
     * @return A new descriptor with modified fields.
     */
    CopyOnWriteDescriptor setFields(final Map<String, ?> values);

    /**
     * Returns cloned descriptor with modified fields.
     * @param values A fields to put into the new descriptor.
     * @return A new descriptor with modified fields.
     */
    CopyOnWriteDescriptor setFields(final Descriptor values);
}
