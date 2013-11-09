package com.snamp.connectors;

/**
 * Represents factory for the management entity types.
 * @param <E> Type of the management entity.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see EntityTypeInfoBuilder
 */
public interface EntityTypeInfoFactory<E extends EntityTypeInfo> {
    /**
     * Creates a new management entity type for the specified source-specific type.
     * @param sourceType The source-specific type.
     * @param destinationType The SNAMP-compliant type.
     * @return A new management entity type.
     */
    public E createTypeInfo(final Class<?> sourceType, final Class<?> destinationType);
}
