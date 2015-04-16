package org.jivesoftware.smackx.pep.packet;

import com.itworks.snamp.adapters.xmpp.XMPPNotificationPayload;
import org.jivesoftware.smack.util.StringUtils;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.xml.bind.JAXBException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;

/**
 * Represents XMPP PEP item for the notification.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XMPPNotificationItem extends PEPItem {
    private final XMPPNotificationPayload payload;

    public XMPPNotificationItem(final XMPPNotificationPayload payload) {
        super(StringUtils.randomString(12));
        this.payload = Objects.requireNonNull(payload);
    }

    public XMPPNotificationItem(final Notification n, final MBeanNotificationInfo metadata){
        this(new XMPPNotificationPayload(n, metadata));
    }

    @Override
    public String getNode(){
        return XMPPNotificationPayload.NAMESPACE;
    }

    @Override
    public String getItemDetailsXML() {
        try {
            return payload.toXML();
        } catch (final JAXBException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
