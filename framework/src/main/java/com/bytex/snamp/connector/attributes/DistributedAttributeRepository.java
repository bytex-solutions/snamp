package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.core.Communicator;
import org.osgi.framework.BundleContext;

import javax.management.MBeanAttributeInfo;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents repository for attributes which state should be synchronized across cluster nodes.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 * @see AttributesDistributionJob
 */
public abstract class DistributedAttributeRepository<M extends MBeanAttributeInfo> extends AbstractAttributeRepository<M> implements Consumer<Communicator.IncomingMessage> {

    /**
     * Represents serializable snapshot of the attribute's internal state.
     */
    protected interface AttributeSnapshot extends Serializable{
        /**
         * Gets name of attribute which produces this checkpoint
         * @return Name of attribute which produces this checkpoint.
         */
        String getName();
    }

    protected final ExecutorService threadPool;

    protected DistributedAttributeRepository(final String resourceName,
                                             final Class<M> attributeMetadataType,
                                             final boolean expandable,
                                             final ExecutorService threadPool) {
        super(resourceName, attributeMetadataType, expandable);
        this.threadPool = Objects.requireNonNull(threadPool);
    }

    private void sendSnapshot(final M attribute, final Communicator communicator){
        takeSnapshot(attribute).ifPresent(communicator::sendSignal);
    }

    final void sendSnapshots(final Communicator communicator) {
        parallelForEach(attribute -> sendSnapshot(attribute, communicator), threadPool);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param incomingMessage the input argument
     */
    @Override
    public final void accept(final Communicator.IncomingMessage incomingMessage) {
        final AttributeSnapshot snapshot = (AttributeSnapshot) incomingMessage.getPayload();
        final M attribute = get(snapshot.getName());
        if(attribute != null)
            applySnapshot(attribute, snapshot);
    }

    final BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    protected abstract Optional<AttributeSnapshot> takeSnapshot(final M attribute);

    protected abstract boolean applySnapshot(final M attribute, final AttributeSnapshot snapshot);
}
