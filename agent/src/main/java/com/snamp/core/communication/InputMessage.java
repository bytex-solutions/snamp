package com.snamp.core.communication;

import java.util.Date;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface InputMessage<REQ, RES> {
    /**
     * Gets descriptor of this input message.
     * @return The descriptor of this input message.
     */
    MessageDescriptor<REQ, RES> getDescriptor();

    /**
     * Gets timestamp of this message.
     * @return The creation time of this object.
     */
    Date getTimestamp();

    /**
     * Gets uniquely generated message ID.
     * @return The message ID.
     */
    long getMessageID();

    /**
     * Gets correlation ID that is used to support dialogs.
     * @return The correlation ID.
     */
    long getCorrelationID();

    /**
     * Gets payload of this message.
     * @return Gets payload of this message.
     */
    REQ getPayload();
}
