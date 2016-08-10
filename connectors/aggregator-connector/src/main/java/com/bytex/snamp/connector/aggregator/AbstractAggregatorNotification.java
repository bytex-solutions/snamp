package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.CustomNotificationInfo;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.connector.aggregator.AggregatorConnectorConfiguration.getForeignAttributeName;
import static com.bytex.snamp.connector.aggregator.AggregatorConnectorConfiguration.getSourceManagedResource;
import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
abstract class AbstractAggregatorNotification extends CustomNotificationInfo {
    private static final long serialVersionUID = -848996422871723373L;
    private final String source;
    private final Logger logger;

    /**
     * The name of the imported attribute.
     */
    final String foreignAttribute;

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


    final void attributeNotFound(final String attributeName, final AttributeNotFoundException e) {
        logger.log(Level.WARNING, String.format("Unknown attribute '%s'", attributeName), e);
    }

    final void failedToGetAttribute(final String attributeName,
                                    final Exception e) {
        logger.log(Level.SEVERE, String.format("Can't read '%s' attribute", attributeName), e);
    }

    protected abstract void process(final AttributeSupport attributes,
                                    final NotificationEnqueue sender);

    static EventConfiguration createEventConfiguration(final ClassLoader context){
        return ConfigurationManager.createEntityConfiguration(context, EventConfiguration.class);
    }

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
