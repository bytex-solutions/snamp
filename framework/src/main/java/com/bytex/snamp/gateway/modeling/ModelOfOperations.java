package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.EntryReader;

import javax.management.MBeanOperationInfo;
import java.util.Collection;

/**
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 1.0
 */
public abstract class ModelOfOperations<TAccessor extends OperationAccessor> extends ModelOfFeatures<MBeanOperationInfo, TAccessor, ResourceOperationList<TAccessor>> implements OperationSet<TAccessor> {
    /**
     * Initializes a new thread-safe object.
     *
     * @param resourceGroupDef The type of the enum which represents a set of field groups.
     * @param <G> Enum definition.
     */
    protected <G extends Enum<G>> ModelOfOperations(final Class<G> resourceGroupDef, final Enum<G> listGroup) {
        super(ResourceOperationList::new, resourceGroupDef, listGroup);
    }

    /**
     * Initializes a new thread-safe object in which all fields represents the single resource.
     */
    protected ModelOfOperations() {
        this(SingleResourceGroup.class, SingleResourceGroup.INSTANCE);
    }

    @Override
    protected abstract TAccessor createAccessor(final String resourceName, final MBeanOperationInfo metadata) throws Exception;

    public final <E extends Throwable> boolean forEachOperation(final EntryReader<String, ? super TAccessor, E> operationReader) throws E{
        return forEachFeature(operationReader);
    }

    public final Collection<MBeanOperationInfo> getResourceOperationsMetadata(final String resourceName){
        return getResourceFeaturesMetadata(resourceName);
    }

    public final <E extends Throwable> boolean processOperation(final String resourceName,
                                                                   final String operationName,
                                                                   final Acceptor<? super TAccessor, E> processor) throws E, InterruptedException {
        return processFeature(resourceName, operationName, processor);
    }
}
