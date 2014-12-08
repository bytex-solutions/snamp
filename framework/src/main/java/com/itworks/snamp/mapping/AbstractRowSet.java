package com.itworks.snamp.mapping;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Represents an abstract class for constructing row sets.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractRowSet<C> extends OrdinalRecordSet<Integer, RecordSet<String, C>> implements RowSet<C> {
    private static abstract class ProxyRowSet<C> implements RowSet<C>{
        protected final AbstractRowSet<C> rowSet;

        private ProxyRowSet(final AbstractRowSet<C> parent){
            this.rowSet = parent;
        }

        @Override
        public final int size() {
            return rowSet.size();
        }

        @Override
        public final Set<String> getColumns() {
            return rowSet.getColumns();
        }

        @Override
        public final boolean isIndexed(final String columnName) {
            return rowSet.isIndexed(columnName);
        }

        @Override
        public final RowSet<C> parallel(final ExecutorService executor) {
            return rowSet.parallel(executor);
        }

        @Override
        public final RowSet<C> sequential() {
            return rowSet.sequential();
        }
    }

    /**
     * Gets index of the first element.
     *
     * @return Index of the first element; or {@literal null}, if record set is empty.
     */
    @Override
    protected final Integer first() {
        return 0;
    }

    /**
     * Computes index of the next record.
     *
     * @param rowIndex Index of the previous record.
     * @return Index of the next record; or {@link null}, if the specified index represents the last record.
     */
    @Override
    protected final Integer next(final Integer rowIndex) {
        return rowIndex < size() - 1 ? rowIndex + 1 : null;
    }

    protected abstract C getCell(final String columnName, final int rowIndex);

    /**
     * Gets record at the specified index.
     *
     * @param rowIndex The index of the record.
     * @return A record at the specified index.
     */
    @Override
    protected final KeyedRecordSet<String, C> getRecord(final Integer rowIndex) {
        return new KeyedRecordSet<String, C>() {
            @Override
            protected Set<String> getKeys() {
                return AbstractRowSet.this.getColumns();
            }

            @Override
            protected C getRecord(final String columnName) {
                return getCell(columnName, rowIndex);
            }
        };
    }

    private static <C> ProxyRowSet<C> sequential(final AbstractRowSet<C> parent){
        return new ProxyRowSet<C>(parent) {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super Integer, ? super RecordSet<String, C>, E> reader) throws E {
                rowSet.forEachSequential(reader);
            }
        };
    }

    private static <C> ProxyRowSet<C> parallel(final AbstractRowSet<C> parent,
                                               final ExecutorService executor){
        return new ProxyRowSet<C>(parent) {
            @Override
            public <E extends Exception> void forEach(final RecordReader<? super Integer, ? super RecordSet<String, C>, E> reader) throws E {
                rowSet.forEachParallel(reader, executor);
            }
        };
    }

    /**
     * Returns an equivalent object that is sequential.
     * May return itself, either because the object was already sequential,
     * or because the underlying object state was modified to be sequential.
     *
     * @return An object that supports sequential execution of some methods.
     */
    @Override
    public RowSet<C> sequential() {
        return sequential(this);
    }

    /**
     * Returns an equivalent object that is parallel.
     * May return itself, either because the object was already parallel,
     * or because the underlying object state was modified to be parallel.
     *
     * @param executor An executor used to execute methods in parallel manner.
     * @return An object that supports parallel execution of some methods.
     */
    @Override
    public RowSet<C> parallel(final ExecutorService executor) {
        return parallel(this, executor);
    }

}
