package com.bytex.snamp.io;

import java.io.ObjectStreamClass;

/**
 * Represents class resolver using during deserialization.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.2
 */
@FunctionalInterface
public interface ClassResolver {
    /**
     * Load the local class equivalent of the specified stream class
     * description.
     * @param desc Serialized information about class. Cannot be {@literal null}.
     * @return Resolved class.
     * @throws ClassNotFoundException if class of a serialized object cannot be found.
     */
    Class<?> resolveClass(final ObjectStreamClass desc)
            throws ClassNotFoundException;

    static ClassResolver forClassLoader(final ClassLoader loader) {
        return desc -> Class.forName(desc.getName(), false, loader);
    }
}
