package com.bytex.snamp.connector.mda.impl.thrift;

import com.bytex.snamp.Acceptor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import java.util.regex.Pattern;

/**
 * Represents Thrift message type.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
enum MessageType {
    GET_ATTRIBUTE,
    SET_ATTRIBUTE,
    SEND_NOTIFICATION{
        @Override
        void beginResponse(final TProtocol output, final String name, final int seqid) {

        }

        @Override
        void endResponse(final TProtocol output) {

        }
    },
    RESET{
        @Override
        void beginResponse(final TProtocol output, final String name, final int seqid) {
        }

        @Override
        void endResponse(final TProtocol output) {
        }
    };

    void beginResponse(final TProtocol output, final String name, final int seqid) throws TException {
        output.writeMessageBegin(new TMessage(name, TMessageType.REPLY, seqid));
    }

    void endResponse(final TProtocol output) throws TException {
        output.writeMessageEnd();
        output.getTransport().flush();
    }

    private static final String GETTER_MESSAGE = "get_";
    private static final Pattern GETTER_PREFIX = Pattern.compile(GETTER_MESSAGE);

    private static final String SETTER_MESSAGE = "set_";
    private static final Pattern SETTER_PREFIX = Pattern.compile(SETTER_MESSAGE);

    private static final String NOTIFY_MESSAGE = "notify_";
    private static final Pattern NOTIFY_PREFIX = Pattern.compile(NOTIFY_MESSAGE);

    private static final String RESET_MESSAGE = "reset";

    /**
     * Parses message type.
     * @param message Input message to be processed.
     * @param entityName A name of the attribute or notification extracted from the message.
     * @return Message type.
     * @throws E Unable to set entity name.
     */
    static <E extends Throwable> MessageType get(final TMessage message, final Acceptor<String, E> entityName) throws E{
        if (message.name.startsWith(GETTER_MESSAGE)) {
            entityName.accept(GETTER_PREFIX.matcher(message.name).replaceFirst(""));
            return GET_ATTRIBUTE;
        } else if (message.name.startsWith(SETTER_MESSAGE)) {
            entityName.accept(SETTER_PREFIX.matcher(message.name).replaceFirst(""));
            return SET_ATTRIBUTE;
        } else if (message.name.startsWith(NOTIFY_MESSAGE)) {
            entityName.accept(NOTIFY_PREFIX.matcher(message.name).replaceFirst(""));
            return SEND_NOTIFICATION;
        } else if(RESET_MESSAGE.equals(message.name)){
            entityName.accept(null);
            return RESET;
        }
        else return null;
    }
}
