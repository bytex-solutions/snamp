package com.itworks.snamp.mapping;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class FilteredRowReader<I, R, E extends Exception> implements RecordReader<I, R, E> {

    protected abstract boolean isAllowed(final I index);

    protected abstract void readCore(final I index, final R value) throws E;

    /**
     * Processes the single record.
     *
     * @param index An index of the record.
     * @param value A record.
     * @throws E Unable to process record.
     */
    @Override
    public final void read(final I index, final R value) throws E {
        if(isAllowed(index)) readCore(index, value);
    }
}
