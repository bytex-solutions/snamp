package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.internal.Utils;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.io.PrintStream;
import java.util.logging.Logger;

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
}
