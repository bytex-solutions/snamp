package com.bytex.snamp.io;

import java.io.IOException;
import java.io.ObjectStreamClass;

/**
 * Represents class resolver using during deserialization.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
@FunctionalInterface
public interface ClassResolver {
    /**
     * Load the local class equivalent of the specified stream class
     * description.
     * @param desc Serialized information about class. Cannot be {@literal null}.
     * @return Resolved class.
     * @throws IOException any of the usual Input/Output exceptions.
     * @throws ClassNotFoundException if class of a serialized object cannot be found.
     */
    Class<?> resolveClass(final ObjectStreamClass desc)
            throws IOException, ClassNotFoundException;
}
