package com.itworks.snamp.connectors.aggregator;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeSupport;
import com.itworks.snamp.connectors.notifications.CustomNotificationInfo;
import com.itworks.snamp.connectors.notifications.NotificationDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.DescriptorUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.*;
import java.io.IOException;

import static com.itworks.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getForeignAttributeName;
import static com.itworks.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getSourceManagedResource;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class PeriodicAttributeQuery extends CustomNotificationInfo {
    private static final String DESCRIPTION = "Broadcasts attribute value in periodic manner";
    private static final long serialVersionUID = -3815002481131666409L;

    private final String source;
    private final String foreignAttribute;

    PeriodicAttributeQuery(final String notifType,
                                  final NotificationDescriptor descriptor) throws AbsentAggregatorNotificationParameterException {
        super(notifType, DESCRIPTION, descriptor);
        source = getSourceManagedResource(descriptor);
        foreignAttribute = getForeignAttributeName(descriptor);
    }

    public String getForeignAttribute(){
        return foreignAttribute;
    }

    private ServiceReferenceHolder<ManagedResourceConnector> getResource(final BundleContext context) throws InstanceNotFoundException {
        final ServiceReference<ManagedResourceConnector> resourceRef =
                ManagedResourceConnectorClient.getResourceConnector(context, source);
        if(resourceRef != null)
            return new ServiceReferenceHolder<>(context, resourceRef);
        else throw new InstanceNotFoundException(String.format("Managed resource %s not found", source));
    }

    private NotificationSurrogate createNotification(final AttributeSupport connector) throws MBeanException, AttributeNotFoundException, ReflectionException, IOException {
        if(connector == null) return null;
        final MBeanAttributeInfo metadata = connector.getAttributeInfo(foreignAttribute);
        if(metadata == null) return null;
        return new NotificationSurrogate(DescriptorUtils.toString(metadata.getDescriptor(), "Source attribute properties"),
                        connector.getAttribute(foreignAttribute)
                );
    }

    NotificationSurrogate createNotification() throws InstanceNotFoundException, MBeanException, AttributeNotFoundException, ReflectionException, IOException {
        final BundleContext context = Utils.getBundleContextByObject(this);
        final ServiceReferenceHolder<ManagedResourceConnector> connector = getResource(context);
        try{
            return createNotification(connector.get().queryObject(AttributeSupport.class));
        } finally {
            connector.release(context);
        }
    }
}
