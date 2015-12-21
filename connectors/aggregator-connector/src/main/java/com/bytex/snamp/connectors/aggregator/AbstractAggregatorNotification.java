package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.notifications.CustomNotificationInfo;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getForeignAttributeName;
import static com.bytex.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getSourceManagedResource;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAggregatorNotification extends CustomNotificationInfo {
    private final String source;
    private final Logger logger;

    /**
     * The name of the imported attribute.
     */
    protected final String foreignAttribute;

    AbstractAggregatorNotification(final String notifType,
                                   final String description,
                           final NotificationDescriptor descriptor,
                                   final Logger logger) throws AbsentAggregatorNotificationParameterException {
        super(notifType, description, descriptor);
        source = getSourceManagedResource(descriptor);
        foreignAttribute = getForeignAttributeName(descriptor);
        this.logger = Objects.requireNonNull(logger);
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }


    protected final void attributeNotFound(final String attributeName, final AttributeNotFoundException e) {
        logger.log(Level.WARNING, String.format("Unknown attribute '%s'", attributeName), e);
    }

    protected final void failedToGetAttribute(final String attributeName,
                                      final Exception e) {
        logger.log(Level.SEVERE, String.format("Can't read '%s' attribute", attributeName), e);
    }

    protected abstract void process(final AttributeSupport attributes,
                                    final NotificationEnqueue sender);

    final void process(final NotificationEnqueue sender) throws InstanceNotFoundException {
        final BundleContext context = getBundleContext();
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, source);
        try {
            process(client.queryObject(AttributeSupport.class), sender);
        } finally {
            client.release(context);
        }
    }
}
