package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.EntryReader;

/**
 * Represents reader for a set of events stored inside of the gateway instance.
 * @param <TAccessor> Type of the operation accessor.
 * @author Evgeniy Kirichenko
 * @version 2.1
 * @since 1.0
 */
public interface OperationSet<TAccessor extends OperationAccessor> {
    /**
     * Iterates over all operations in this set.
     * @param operationReader operation reader.
     * @param <E> Type of the exception that can be thrown by the reader.
     * @return {@literal false}, if iteration was aborted.
     * @throws E Unable to process operation.
     */
    <E extends Throwable> boolean forEachOperation(final EntryReader<String, ? super TAccessor, E> operationReader) throws E;
}
