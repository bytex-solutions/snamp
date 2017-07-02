package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMemberInfo;
import com.bytex.snamp.core.Communicator;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class TransferObject implements Serializable {
    private static final long serialVersionUID = -1700062098464408974L;
    final String senderName;
    final Serializable payload;
    final boolean isSenderActive;
    final long messageID;
    final Communicator.MessageType messageType;

    TransferObject(final ClusterMemberInfo memberInfo, final Serializable payload, final Communicator.MessageType type, final long messageID){
        this.senderName = memberInfo.getName();
        this.isSenderActive = memberInfo.isActive();
        this.payload = Objects.requireNonNull(payload);
        this.messageID = messageID;
        this.messageType = type;
    }

    @Override
    public String toString() {
        return payload.toString();
    }
}
