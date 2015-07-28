package com.bytex.snamp.adapters.xmpp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.bytex.snamp.StringAppender;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.modeling.NotificationRouter;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.jmx.json.Formatters;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.util.Collection;
import java.util.Map;

/**
 * Bridge between notifications and XMPP protocol.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPNotificationAccessor extends NotificationRouter {
    static final String LISTEN_COMMAND_PATTERN = "notifs %s";
    final String resourceName;
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
        if(XMPPAdapterConfigurationProvider.isM2MEnabled(metadata.getDescriptor())) {
            final JivePropertiesExtension extension = new JivePropertiesExtension();
            XMPPUtils.copyDescriptorFields(metadata.getDescriptor(), extension);
            extensions.add(extension);
        }
    }

    static String toString(final Notification notif){
        return FORMATTER.toJson(notif);
    }

    final String getListenCommand(){
        final Map<String, ?> filterParams = DescriptorUtils.toMap(getDescriptor());
        switch (filterParams.size()){
            case 0: return String.format(LISTEN_COMMAND_PATTERN, "");
            case 1:
                for(final Map.Entry<String, ?> entry: filterParams.entrySet())
                    return String.format(LISTEN_COMMAND_PATTERN, String.format("(%s=%s)", entry.getKey(), entry.getValue()));
            default:
                final StringAppender filter = new StringAppender(30);
                for(final Map.Entry<String, ?> entry: filterParams.entrySet())
                    filter.append(null, "(%s=%s)", entry.getKey(), entry.getValue());
                return String.format("(&(%s))", filter);
        }
    }
}