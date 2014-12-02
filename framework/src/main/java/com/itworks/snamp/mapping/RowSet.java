package com.itworks.snamp.mapping;

import java.util.Set;

/**
 * Represents a set of table rows.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface RowSet<C> extends RecordSet<Integer, RecordSet<String, C>> {
    /**
     * Gets the number of rows.
     *
     * @return The number of rows.
     */
    @Override
    int size();

    /**
     * Gets a set of table columns.
     * @return A set of table columns.
     */
    Set<String> getColumns();
}
