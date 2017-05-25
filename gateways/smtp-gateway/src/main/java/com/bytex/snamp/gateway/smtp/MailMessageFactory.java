package com.bytex.snamp.gateway.smtp;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.CompiledST;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.management.MBeanNotificationInfo;

/**
 * Represents factory of e-mail message.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface MailMessageFactory {
    Message createMessage() throws MessagingException;
    ST prepareHealthStatusTemplate();

    ST prepareNewResourceTemplate();

    ST prepareRemovedResourceTemplate();

    ST prepareScaleOutTemplate();

    ST prepareScaleInTemplate();

    ST prepareMaxClusterSizeReachedTemplate();

    CompiledST compileNotificationTemplate(final MBeanNotificationInfo metadata);
}
