package com.itworks.snamp.adapters.snmp.runtime;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.itworks.snamp.adapters.NotificationAccessor;
import com.itworks.snamp.adapters.runtime.EventBinding;
import com.itworks.snamp.adapters.snmp.SnmpNotificationAcessor;
import com.itworks.snamp.adapters.snmp.SnmpNotificationMapping;
import com.itworks.snamp.adapters.snmp.SnmpType;
import com.itworks.snamp.jmx.WellKnownType;

import java.util.Collection;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpNotificationBinding extends EventBinding {
    private final SnmpType attachmentType;

    SnmpNotificationBinding(final String declaredResource,
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
