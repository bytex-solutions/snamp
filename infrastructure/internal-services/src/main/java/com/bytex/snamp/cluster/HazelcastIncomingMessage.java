package com.bytex.snamp.cluster;

import com.bytex.snamp.core.Communicator;
import com.hazelcast.core.Message;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents serializable message.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class HazelcastIncomingMessage extends Communicator.MessageEvent {
    private static final long serialVersionUID = -4238764016229125147L;
    private final Message<TransferObject> hzMessage;
    private boolean remote;

    HazelcastIncomingMessage(final Message<TransferObject> hzMessage){
        super(getHazelcastNodeInfo(hzMessage));
        this.hzMessage = Objects.requireNonNull(hzMessage);
    }

    private static HazelcastNodeInfo getHazelcastNodeInfo(final Message<TransferObject> hzMessage){
        return new HazelcastNodeInfo(hzMessage.getPublishingMember(), hzMessage.getMessageObject().isSenderActive, hzMessage.getMessageObject().senderName);
    }

    /**
     * Gets sender of the message.
     *
     * @return Sender of the message; or {@literal null} when communicator is not in cluster.
     */
    @Override
    public HazelcastNodeInfo getSource() {
        return (HazelcastNodeInfo) super.getSource();
    }

    /**
     * Gets payload of the message.
     *
     * @return Payload of the message.
     */
    @Override
    public Serializable getPayload() {
        return hzMessage.getMessageObject().payload;
    }

    /**
     * Gets message identifier.
     *
     * @return Message identifier.
     */
    @Override
    public long getMessageID() {
        return hzMessage.getMessageObject().messageID;
    }

    /**
     * Gets publication time of this message in Unix time format.
     *
     * @return Publication time of this message in Unix time format.
     */
    @Override
    public long getTimeStamp() {
        return hzMessage.getPublishTime();
    }

    /**
     * Gets type of this message.
     *
     * @return Type of this message.
     */
    @Override
    public Communicator.MessageType getType() {
        return hzMessage.getMessageObject().messageType;
    }

    @Override
    public boolean isRemote() {
        return remote;
    }

    void detectRemoteMessage(final String localNodeID){
        final boolean isLocal = localNodeID.equals(getSource().getNodeID());
        remote = !isLocal;
    }
}
