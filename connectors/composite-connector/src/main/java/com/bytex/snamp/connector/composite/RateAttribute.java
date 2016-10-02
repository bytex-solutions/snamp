package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.metrics.RateRecorder;
import com.bytex.snamp.jmx.MetricsConverter;

import javax.management.MBeanException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;

/**
 * Represents attribute computing rate of the input notifications.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class RateAttribute extends AbstractCompositeAttribute implements NotificationListener {
    private static final long serialVersionUID = -6525078880291154173L;
    private static final CompositeType TYPE = MetricsConverter.RATE_TYPE;
    private static final String DESCRIPTION = "Computes rate of the notification";

    private final RateRecorder rate;
    private final String notificationType;

    RateAttribute(final String name, final AttributeDescriptor descriptor) {
        super(name, TYPE.getClassName(), descriptor.getDescription(DESCRIPTION), true, false, false, descriptor);
        rate = new RateRecorder(name);
        notificationType = descriptor.getName(name);
    }

    @Override
    public void handleNotification(final Notification notification, final Object handback) {
        if(this.notificationType.equals(notification.getType()))
            rate.mark();
    }

    @Override
    CompositeData getValue(final AttributeSupportProvider provider) throws Exception {
        return MetricsConverter.fromRate(rate);
    }

    @Override
    void setValue(final AttributeSupportProvider provider, final Object value) throws MBeanException {
        throw new MBeanException(new UnsupportedOperationException(String.format("Attribute %s is read-only", getName())));
    }
}
