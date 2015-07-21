package com.itworks.snamp.adapters.xmpp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.adapters.modeling.NotificationRouter;
import com.itworks.snamp.jmx.json.Formatters;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Collection;

/**
 * Bridge between notifications and XMPP protocol.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPNotificationAccessor extends NotificationRouter {
    private final String resourceName;
    private static final Gson FORMATTER = Formatters.enableAll(new GsonBuilder())
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    XMPPNotificationAccessor(final MBeanNotificationInfo metadata,
                             final NotificationListener listener,
                             final String resourceName) {
        super(metadata, listener);
        this.resourceName = resourceName;
    }

    @Override
    protected Notification intercept(final Notification notification) {
        notification.setSource(resourceName);
        return notification;
    }

    static void createExtensions(final MBeanNotificationInfo metadata,
                                 final Collection<ExtensionElement> extensions){
        if(XMPPAdapterConfiguration.isM2MEnabled(metadata.getDescriptor())) {
            final JivePropertiesExtension extension = new JivePropertiesExtension();
            XMPPUtils.copyDescriptorFields(metadata.getDescriptor(), extension);
            extensions.add(extension);
        }
    }

    static String toString(final Notification notif){
        return FORMATTER.toJson(notif);
    }
}
