package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.NotificationRouter;
import com.bytex.snamp.jmx.DescriptorUtils;
import com.bytex.snamp.json.JsonUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.jiveproperties.packet.JivePropertiesExtension;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;

/**
 * Bridge between notifications and XMPP protocol.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class XMPPNotificationAccessor extends NotificationRouter {
    static final String LISTEN_COMMAND_PATTERN = "notifs %s";
    private static final ObjectWriter FORMATTER;

    static {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.registerModule(new JsonUtils());
        FORMATTER = mapper.writer();
    }

    XMPPNotificationAccessor(final MBeanNotificationInfo metadata,
                             final NotificationListener listener,
                             final String resourceName) {
        super(resourceName, metadata, listener);
    }

    static void createExtensions(final MBeanNotificationInfo metadata,
                                 final Collection<ExtensionElement> extensions){
        if(XMPPGatewayConfigurationProvider.isM2MEnabled(metadata.getDescriptor())) {
            final JivePropertiesExtension extension = new JivePropertiesExtension();
            XMPPUtils.copyDescriptorFields(metadata.getDescriptor(), extension);
            extensions.add(extension);
        }
    }

    static String toString(final Notification notif) {
        try {
            return FORMATTER.writeValueAsString(notif);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String getListenCommand(){
        final Map<String, ?> filterParams = DescriptorUtils.toMap(getDescriptor());
        switch (filterParams.size()) {
            case 0:
                return String.format(LISTEN_COMMAND_PATTERN, "");
            case 1:
                return filterParams.entrySet()
                        .stream()
                        .map(entry -> String.format(LISTEN_COMMAND_PATTERN, String.format("(%s=%s)", entry.getKey(), entry.getValue())))
                        .findFirst()
                        .orElse(null);
            default:
                final StringBuilder filter = new StringBuilder(30);
                for (final Map.Entry<String, ?> entry : filterParams.entrySet())
                    filter.append(String.format("(%s=%s)", entry.getKey(), entry.getValue()));
                return String.format("(&(%s))", filter);

        }
    }
}
