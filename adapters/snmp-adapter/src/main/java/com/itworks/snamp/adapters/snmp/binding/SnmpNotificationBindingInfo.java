package com.itworks.snamp.adapters.snmp.binding;

import com.google.common.base.Function;
import com.itworks.snamp.adapters.snmp.SnmpNotificationAcessor;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpNotificationBindingInfo extends NotificationBindingInfo {
    private final SnmpType attachmentType;

    SnmpNotificationBindingInfo(final String declaredResource,
                                final SnmpNotificationAcessor accessor,
                                final Function<WellKnownType, SnmpType> typeMapper) {
        super(declaredResource, accessor);
        put("OID", accessor.getID());
        attachmentType = accessor.getType(typeMapper);
    }

    /**
     * Gets information about attachment type of this event binding.
     *
     * @return The information about attachment type of this event binding.
     */
    @Override
    public SnmpType getAttachmentType() {
        return attachmentType;
    }
}
