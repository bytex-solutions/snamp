package com.snamp.core.communication;

/**
 * Thrown to indicate that the input message is not supported.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class MessageNotSupportedException extends UnsupportedOperationException {
    public MessageNotSupportedException(){
        super("Message is not supported.");
    }
}
