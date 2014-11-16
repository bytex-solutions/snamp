package com.itworks.snamp.adapters.ssh;

/**
 * Represents binding between SSH server sessions and native SNAMP notification model.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SshNotificationView {
    String getEventName();

    String getResourceName();
}
