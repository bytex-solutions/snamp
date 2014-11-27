package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.connectors.ManagedEntityType;

import javax.management.Notification;
import java.util.Map;

interface AttachmentResolver {
    ManagedEntityType resolveType(final JmxTypeSystem typeSystem);

    Object extractAttachment(final Notification notif, final JmxTypeSystem typeSystem);

    void exposeTypeInfo(final Map<String, String> options);
}
