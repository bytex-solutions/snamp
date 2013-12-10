package com.snamp.connectors;

import java.util.Collection;

/**
 * Represents a table type of the management entity.
 * <p>
 *     It is highly recommended to represent tabular management type as
 *     an instance of the {@link ManagementEntityType} that implements this interface.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagementEntityTabularType extends ManagementEntityType {
    /**
     * Returns a set of column names.
     * @return The set of column names.
     */
    public Collection<String> getColumns();

    /**
     * Returns the column type.
     * @param column The name of the column.
     * @return The type of the column; or {@literal null} if the specified column doesn't exist.
     */
    public ManagementEntityType getColumnType(final String column);

    /**
     * Returns the number of rows if this information is available.
     * @return The count of rows.
     * @throws UnsupportedOperationException Row count is not supported.
     */
    public long getRowCount() throws UnsupportedOperationException;
}
