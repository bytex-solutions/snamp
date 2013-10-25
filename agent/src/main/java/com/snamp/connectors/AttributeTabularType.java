package com.snamp.connectors;

import java.util.*;

/**
 * Represents a table type of the attribute value.
 * @author roman
 */
public interface AttributeTabularType extends AttributeTypeInfo {

    /**
     * Returns a set of column names.
     * @return
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
