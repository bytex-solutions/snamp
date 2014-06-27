package com.itworks.snamp.connectors;

import com.itworks.snamp.TypeConverter;

/**
 * Describes type of the management entity, such as attribute or notification attachment.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagementEntityType {
    /**
     * Returns the type converter for the specified native Java type mapping.
     * @param projectionType The native Java type to which the entity value can be converted. Cannot be {@literal null}.
     * @param <T> Type of the projection.
     * @return The type converter for the specified projection type; or {@literal null}, if projection is not supported.
     */
    <T> TypeConverter<T> getProjection(final Class<T> projectionType);
}
