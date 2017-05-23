package com.bytex.snamp.gateway.smtp;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.concurrent.Callable;

/**
 * Represents factory of e-mail message.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface MailMessageFactory extends Callable<Message> {
    @Override
    Message call() throws MessagingException;
}
