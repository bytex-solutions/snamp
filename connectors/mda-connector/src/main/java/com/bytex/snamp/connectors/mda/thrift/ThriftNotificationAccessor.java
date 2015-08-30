package com.bytex.snamp.connectors.mda.thrift;


import com.bytex.snamp.connectors.notifications.CustomNotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.jmx.WellKnownType;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import javax.management.openmbean.*;

import static com.bytex.snamp.connectors.mda.MDAResourceConfigurationDescriptorProvider.parseType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class ThriftNotificationAccessor extends CustomNotificationInfo {
    private final ThriftValueParser attachmentParser;

    ThriftNotificationAccessor(final String notifType,
                             final NotificationDescriptor descriptor) throws OpenDataException {
        super(notifType, descriptor.getDescription(descriptor.getNotificationCategory()), descriptor);
        final OpenType<?> attachmentType = parseType(descriptor);
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
