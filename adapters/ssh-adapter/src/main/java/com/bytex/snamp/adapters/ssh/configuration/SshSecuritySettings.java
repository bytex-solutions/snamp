package com.bytex.snamp.adapters.ssh.configuration;

import net.schmizz.sshj.userauth.keyprovider.KeyFormat;

import java.security.InvalidKeyException;
import java.security.PublicKey;

/**
 * Represents SSH security settings.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public interface SshSecuritySettings {

    String getUserName();
    String getPassword();
    boolean hasUserCredentials();

    String getJaasDomain();
    boolean hasJaasDomain();

    boolean hasClientPublicKey();
    PublicKey getClientPublicKey() throws InvalidKeyException;
    KeyFormat getClientPublicKeyFormat();
}
