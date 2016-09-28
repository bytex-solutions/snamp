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
final class HazelcastIncomingMessage implements Communicator.IncomingMessage {
    private final HazelcastNodeInfo sender;
    private final Message<TransferObject> hzMessage;

    HazelcastIncomingMessage(final Message<TransferObject> hzMessage){
        this.hzMessage = Objects.requireNonNull(hzMessage);
        this.sender = new HazelcastNodeInfo(hzMessage.getPublishingMember(), hzMessage.getMessageObject().isSenderActive, hzMessage.getMessageObject().senderName);
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
     * Gets sender of the message.
     *
     * @return Sender of the message.
     */
    @Override
    public HazelcastNodeInfo getSender() {
        return sender;
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
}
