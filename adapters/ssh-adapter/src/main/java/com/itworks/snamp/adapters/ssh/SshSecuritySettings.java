package com.itworks.snamp.adapters.ssh;

/**
 * Represents SSH security settings.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface SshSecuritySettings {
    String getUserName();
    String getPassword();
    boolean hasUserCredentials();

    String getJaasDomain();
    boolean hasJaasDomain();
}
