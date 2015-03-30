package com.itworks.snamp.adapters.ssh;

import javax.management.Notification;
import java.io.Writer;

/**
 * Represents binding between SSH server sessions and native SNAMP notification model.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface SshNotificationMapping {
    String getType();
    void print(final Notification notif, final Writer output);
}
