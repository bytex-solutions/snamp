package com.itworks.snamp.mapping;

import java.util.Set;
import java.util.concurrent.ExecutorService;

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

    /**
     * Determines whether the specified column is indexed.
     * @param columnName The column name.
     * @return {@literal true}, if the specified column is indexed; otherwise, {@literal false}.
     */
    boolean isIndexed(final String columnName);

    /**
     * Returns an equivalent object that is parallel.
     * May return itself, either because the object was already parallel,
     * or because the underlying object state was modified to be parallel.
     *
     * @param executor An executor used to execute methods in parallel manner.
     * @return An object that supports parallel execution of some methods.
     */
    @Override
    RowSet<C> parallel(final ExecutorService executor);

    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     *
     * @return An object that supports sequential execution of some methods.
     */
    @Override
    RowSet<C> sequential();
}
