package com.itworks.snamp.adapters.ssh;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshAdapterConfigurationDescriptor {
    static final String HOST_PARAM = "host";
    static final String DEFAULT_HOST = "localhost";

    static final String PORT_PARAM = "port";
    static final int DEFAULT_PORT = 22;

    static final String CERTIFICATE_FILE_PARAM = "certificateFile";
    static final String DEFAULT_CERTIFICATE  = "ssh-adapter.ser";

    static final String JAAS_DOMAIN_PARAM = "jaasDomain";
    static final String USER_NAME_PARAM = "userName";
    static final String PASSWORD_PARAM = "password";


}
