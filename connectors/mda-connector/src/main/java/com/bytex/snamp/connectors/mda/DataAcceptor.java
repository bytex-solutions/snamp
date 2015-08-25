package com.bytex.snamp.connectors.mda;

import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.ManagedResourceConnector;

import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.Set;

/**
 * Represents Monitoring Data Acceptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface DataAcceptor extends ManagedResourceConnector {
    boolean addAttribute(final String attributeID,
                         final String attributeName,
                         final TimeSpan readWriteTimeout,
                         final CompositeData options);

    void removeAttributesExcept(final Set<String> attributes);

    boolean enableNotifications(final String listId, final String category, final CompositeData options);

    void disableNotificationsExcept(final Set<String> notifications);

    void beginAccept(final Object... dependencies) throws IOException;
}
