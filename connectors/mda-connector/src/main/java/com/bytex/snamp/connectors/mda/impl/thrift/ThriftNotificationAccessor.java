package com.bytex.snamp.connectors.mda.impl.thrift;


import com.bytex.snamp.connectors.notifications.CustomNotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.WellKnownType;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import javax.management.openmbean.*;


/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftNotificationAccessor extends CustomNotificationInfo {
    private static final long serialVersionUID = 6089266900618130774L;
    private final ThriftValueParser attachmentParser;

    ThriftNotificationAccessor(final String notifType,
                               final OpenType<?> attachmentType,
                             final NotificationDescriptor descriptor) throws OpenDataException {
        super(notifType, descriptor.getDescription(descriptor.getNotificationCategory()), descriptor);
        if(attachmentType instanceof SimpleType<?> || attachmentType instanceof ArrayType<?>)
            attachmentParser = new SimpleValueParser(WellKnownType.getType(attachmentType));
        else if(attachmentType instanceof CompositeType)
            attachmentParser = new CompositeValueParser((CompositeType)attachmentType);
        else attachmentParser = null;
    }

    Object parseUserData(final TProtocol input) throws TException {
        return attachmentParser != null ? attachmentParser.deserialize(input) : null;
    }
}
