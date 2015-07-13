package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableEventConfiguration;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.CustomNotificationInfo;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.DescriptorUtils;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.io.IOException;
import java.util.logging.Logger;

import static com.itworks.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getForeignAttributeName;
import static com.itworks.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getSourceManagedResource;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class PeriodicAttributeQuery extends AbstractAggregatorNotification {
    private static final String DESCRIPTION = "Broadcasts attribute value in periodic manner";
    static final String CATEGORY = "periodicAttributeQuery";
    private static final long serialVersionUID = -3815002481131666409L;


    PeriodicAttributeQuery(final String notifType,
                           final NotificationDescriptor descriptor,
                           final Logger logger) throws AbsentAggregatorNotificationParameterException {
        super(notifType, DESCRIPTION, descriptor, logger);
    }

    @Override
    protected void process(final AttributeSupport attributes, final NotificationEnqueue sender) {
        final Object attributeValue;
        try {
            attributeValue = attributes.getAttribute(foreignAttribute);
        } catch (final AttributeNotFoundException e) {
            attributeNotFound(foreignAttribute, e);
            return;
        } catch (final JMException e) {
            failedToGetAttribute(foreignAttribute, e);
            return; //any exception must be ignored
        }
        sender.sendNotification(this, "Attribute value = " + attributeValue, attributeValue);
    }

    static SerializableEventConfiguration getConfiguration() {
        final SerializableEventConfiguration result = new SerializableEventConfiguration(CATEGORY);
        result.getParameters().put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        result.getParameters().put(AggregatorConnectorConfiguration.FOREIGN_ATTRIBUTE_PARAM, "");
        return result;
    }
}
