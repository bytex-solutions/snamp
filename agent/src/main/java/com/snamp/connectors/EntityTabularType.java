package com.snamp.connectors;

import java.util.Set;

/**
 * Represents a table type of the management entity.
 * <p>
 *     It is highly recommended to represent tabular management type as
 *     an instance of the {@link EntityTypeInfo} that implements this interface.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface EntityTabularType extends EntityTypeInfo {
    /**
     * Returns a set of column names.
     * @return The set of column names.
     */
    public Set<String> getColumns();

    /**
     * Returns the column type.
     * @param column The name of the column.
     * @return The type of the column; or {@literal null} if the specified column doesn't exist.
     */
    public EntityTypeInfo getColumnType(final String column);

    /**
     * Returns the number of rows if this information is available.
     * @return The count of rows.
     * @throws UnsupportedOperationException Row count is not supported.
     */
    public long getRowCount() throws UnsupportedOperationException;
}
