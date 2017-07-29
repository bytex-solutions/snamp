package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.gateway.modeling.ModelOfNotifications;

import javax.management.MBeanNotificationInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SmtpModelOfNotifications extends ModelOfNotifications<SmtpNotificationSender> {
    private MailMessageFactory messageFactory;

    @Override
    protected SmtpNotificationSender createAccessor(final String resourceName,
                                                    final MBeanNotificationInfo metadata) throws Exception {
        final SmtpNotificationSender sender;
        if (SmtpGatewayConfigurationDescriptionProvider.sendViaEMail(metadata)) {
            sender = new SmtpNotificationSender(resourceName, metadata);
            if (messageFactory != null)
                sender.setMessageFactory(messageFactory);
        } else sender = null;
        return sender;
    }

    void setMessageFactory(final MailMessageFactory factory){
        messageFactory = Objects.requireNonNull(factory);
        forEachNotification((resourceName, sender) -> {
            sender.setMessageFactory(factory);
            return true;
        });
    }

    @Override
    protected void cleared() {
        messageFactory = null;
    }
}
