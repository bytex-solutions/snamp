package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableEventConfiguration;
import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;

import javax.management.*;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getForeignAttributeName;
import static com.bytex.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getSourceManagedResource;

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