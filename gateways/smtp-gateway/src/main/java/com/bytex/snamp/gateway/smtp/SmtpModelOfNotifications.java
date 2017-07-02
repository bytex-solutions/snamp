package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;

import javax.management.MBeanNotificationInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SmtpModelOfNotifications extends ModelOfNotifications<SmtpNotificationSender> implements NotificationListener {
    private MailMessageFactory messageFactory;

    @Override
    protected SmtpNotificationSender createAccessor(final String resourceName,
                                                    final MBeanNotificationInfo metadata) throws Exception {
        final SmtpNotificationSender sender = new SmtpNotificationSender(resourceName, metadata);
        if (messageFactory != null)
            sender.setMessageFactory(messageFactory);
        return sender;
    }

    void setMessageFactory(final MailMessageFactory factory){
        messageFactory = Objects.requireNonNull(factory);
        forEachNotification((resourceName, sender) -> {
            sender.setMessageFactory(factory);
            return true;
        });
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public void handleNotification(final NotificationEvent event) {
        final MailMessageFactory messageFactory = this.messageFactory;
        if(SmtpGatewayConfigurationDescriptionProvider.sendViaEMail(event.getMetadata()) && messageFactory != null){

        }
    }

    @Override
    protected void cleared() {
        messageFactory = null;
    }
}
