package com.itworks.snamp.connectors;

import java.util.Collection;

/**
 * Represents a table type of the management entity.
 * <p>
 *     It is highly recommended to represent tabular management type as
 *     an instance of the {@link ManagedEntityType} that implements this interface.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedEntityTabularType extends ManagedEntityType {
    /**
     * Returns a set of column names.
     * @return The set of column names.
     */
    public Collection<String> getColumns();

    /**
     * Determines whether the specified column is indexed.
     * @param column The name of the column.
     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
     */
    public boolean isIndexed(final String column);

    /**
     * Returns the column type.
     * @param column The name of the column.
     * @return The type of the column; or {@literal null} if the specified column doesn't exist.
     */
    public ManagedEntityType getColumnType(final String column);

    /**
     * Returns the number of rows if this information is available.
     * @return The count of rows.
     * @throws UnsupportedOperationException Row count is not supported.
     */
    public long getRowCount() throws UnsupportedOperationException;
}
