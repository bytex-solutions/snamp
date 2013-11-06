package com.snamp.connectors;

import java.util.*;

/**
 * Represents a table type of the attribute value.
 * <p>
 *     It is highly recommended to represent tabular management attribute type as
 *     an instance of the {@link AttributeTypeInfo} that implements this interface.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface AttributeTabularType extends AttributeTypeInfo {

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
    public AttributeTypeInfo getColumnType(final String column);

    /**
     * Returns the number of rows if this information is available.
     * @return The count of rows.
     * @throws UnsupportedOperationException Row count is not supported.
     */
    public long getRowCount() throws UnsupportedOperationException;
}
