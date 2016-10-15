package com.bytex.snamp.connector.attributes;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.core.Communicator;

import java.time.Duration;
import java.util.Objects;

import static com.bytex.snamp.core.DistributedServices.getDistributedCommunicator;
import static com.bytex.snamp.core.DistributedServices.isActiveNode;

/**
 * Represents synchronization job which periodically synchronize attributes.
 * <p>
 *   This class should be used in conjunction with {@link DistributedAttributeRepository}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AttributesDistributionJob extends Repeater {
    private static final String CHANNEL_PREFIX = "AttributeStateDistributionChannel-";
    private final DistributedAttributeRepository<?> repository;
    private final Communicator communicator;
    private final SafeCloseable subscription;

    public AttributesDistributionJob(final Duration period, final DistributedAttributeRepository<?> repository) {
        super(period);
        this.repository = Objects.requireNonNull(repository);
        this.communicator = getDistributedCommunicator(repository.getBundleContext(), CHANNEL_PREFIX.concat(repository.getResourceName()));
        subscription = communicator.addMessageListener(repository, Communicator.REMOTE_MESSAGE.and(AttributesDistributionJob::isAttributeSnapshot));
    }

    @Override
    protected void stateChanged(final RepeaterState s) {
        if (s == RepeaterState.CLOSED)
            subscription.close();
    }

    private static boolean isAttributeSnapshot(final Communicator.IncomingMessage message){
        return message.getPayload() instanceof DistributedAttributeRepository.AttributeSnapshot;
    }

    @Override
    protected void doAction() {
        if(isActiveNode(repository.getBundleContext())) { //only master node acting as a sender of snapshots
            repository.sendSnapshots(communicator);
        }
    }
}
