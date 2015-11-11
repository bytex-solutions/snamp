package com.bytex.snamp.connectors.mda.impl.thrift;


import com.bytex.snamp.connectors.mda.MDANotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;


/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftNotificationAccessor extends MDANotificationInfo {
    private static final long serialVersionUID = 6089266900618130774L;

    ThriftNotificationAccessor(final String notifType,
                             final NotificationDescriptor descriptor) {
        super(notifType, descriptor);
    }

    Object parseUserData(final TProtocol input) throws TException {
        if (getAttachmentType() != null)
            return ThriftDataConverter.deserialize(getAttachmentType(), input);
        else
            return null;
    }
}
