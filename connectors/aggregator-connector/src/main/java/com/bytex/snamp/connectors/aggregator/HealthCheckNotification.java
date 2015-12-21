package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.attributes.AttributeSupport;
import com.bytex.snamp.connectors.notifications.NotificationDescriptor;
import com.bytex.snamp.internal.Utils;

import javax.management.JMException;
import java.util.logging.Logger;
import static com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableEventConfiguration;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HealthCheckNotification extends AbstractAggregatorNotification {
    private static final String DESCRIPTION = "Checks attribute state";
    static final String CATEGORY = "healthCheck";
    private static final long serialVersionUID = -2913002481131666409L;

    HealthCheckNotification(final String notifType,
                            final NotificationDescriptor descriptor,
                            final Logger logger) throws AbsentAggregatorNotificationParameterException {
        super(notifType, DESCRIPTION, descriptor, logger);
    }

    @Override
    protected void process(final AttributeSupport attributes, final NotificationEnqueue sender) {
        try {
            attributes.getAttribute(foreignAttribute);
        } catch (final JMException e) {
            sender.sendNotification(this, e.getMessage(), Utils.getStackTrace(e));
        }
    }

    static SerializableEventConfiguration getConfiguration() {
        final SerializableEventConfiguration result = new SerializableEventConfiguration();
        result.setAlternativeName(CATEGORY);
        result.getParameters().put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        result.getParameters().put(AggregatorConnectorConfiguration.FOREIGN_ATTRIBUTE_PARAM, "");
        return result;
    }
}
