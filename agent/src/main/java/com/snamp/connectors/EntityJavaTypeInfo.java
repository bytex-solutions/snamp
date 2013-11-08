package com.snamp.connectors;

/**
 * Represents binding between management entity type and Java type.
 * @param <T> Java type associated with the management information base type.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface EntityJavaTypeInfo<T> extends EntityTypeInfo {
    /**
     * Returns the underlying Java class.
     * @return The underlying Java class.
     */
    public Class<T> getNativeClass();
}
