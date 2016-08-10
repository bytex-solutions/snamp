package com.bytex.snamp.connector.aggregator;

import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import java.util.logging.Logger;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.EventConfiguration;

/**
 * @author Roman Sakno
 * @version 2.0
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

    static EventConfiguration getConfiguration() {
        final EventConfiguration result = createEventConfiguration(PeriodicAttributeQuery.class.getClassLoader());
        result.setAlternativeName(CATEGORY);
        result.getParameters().put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        result.getParameters().put(AggregatorConnectorConfiguration.FOREIGN_ATTRIBUTE_PARAM, "");
        return result;
    }
}
