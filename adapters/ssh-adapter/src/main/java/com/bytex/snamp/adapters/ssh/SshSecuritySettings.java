package com.bytex.snamp.adapters.ssh;

import net.schmizz.sshj.userauth.keyprovider.KeyFormat;

import java.security.PublicKey;

/**
 * Represents SSH security settings.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface SshSecuritySettings {

    String getUserName();
    String getPassword();
    boolean hasUserCredentials();

    String getJaasDomain();
    boolean hasJaasDomain();

    boolean hasClientPublicKey();
    PublicKey getClientPublicKey();
    KeyFormat getClientPublicKeyFormat();
}
