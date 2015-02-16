package com.itworks.snamp.jmx;

/**
 * Represents tabular data bean.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface TabularDataBean<R extends CompositeDataBean> extends Iterable<R> {
    /**
     * Adds a row to this table.
     * @param row The row to add.
     */
    void add(final R row);
}
