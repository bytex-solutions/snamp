package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.EntryReader;
import com.bytex.snamp.concurrent.ThreadSafeObject;

/**
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 1.0
 */
public abstract class ModelOfOperations<TAccessor extends OperationAccessor> extends ThreadSafeObject implements OperationSet<TAccessor> {

    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G> Enum definition.
     */
    protected <G extends Enum<G>> ModelOfOperations(final Class<G> resourceGroupDef) {
        super(resourceGroupDef);
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     */
    protected ModelOfOperations() {
        super(SingleResourceGroup.class);
    }

    /**
     * Iterates over all registered operations.
     * @param operationReader
     * @param <E>
     * @throws E
     */
    public abstract <E extends Exception> void forEachOperation(final EntryReader<String, ? super TAccessor, E> operationReader) throws E;

}
