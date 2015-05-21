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

import static com.itworks.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getForeignAttributeName;
import static com.itworks.snamp.connectors.aggregator.AggregatorConnectorConfiguration.getSourceManagedResource;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class PeriodicAttributeQuery extends CustomNotificationInfo {
    private static final String DESCRIPTION = "Broadcasts attribute value in periodic manner";
    static final String CATEGORY = "periodicAttributeQuery";
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
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, source);
        try{
            return createNotification(client.queryObject(AttributeSupport.class));
        } finally {
            client.release(context);
        }
    }

    static SerializableEventConfiguration getConfiguration() {
        final SerializableEventConfiguration result = new SerializableEventConfiguration(CATEGORY);
        result.getParameters().put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        result.getParameters().put(AggregatorConnectorConfiguration.FOREIGN_ATTRIBUTE_PARAM, "");
        return result;
    }
}
