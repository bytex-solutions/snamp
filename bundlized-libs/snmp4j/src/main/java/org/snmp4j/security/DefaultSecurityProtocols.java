package org.snmp4j.security;

/**
 * Represents a set of default security protocols.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class DefaultSecurityProtocols extends SecurityProtocols {
    private static final long serialVersionUID = -4976046491353164997L;
    private volatile static DefaultSecurityProtocols INSTANCE;

    /**
     * Initializes a new set of default security protocols.
     */
    private DefaultSecurityProtocols(){
        addAuthenticationProtocol(new AuthMD5());
        addAuthenticationProtocol(new AuthSHA());

        addPrivacyProtocol(new Priv3DES());
        addPrivacyProtocol(new PrivDES());
        addPrivacyProtocol(new PrivAES128());
        addPrivacyProtocol(new PrivAES192());
        addPrivacyProtocol(new PrivAES256());
    }

    /**
     * Gets singleton instance of this class.
     * @return Singleton instance of this class.
     */
    public static DefaultSecurityProtocols getInstance(){
        DefaultSecurityProtocols result = INSTANCE;
        if(result == null)
            synchronized (DefaultSecurityProtocols.class){
                result = INSTANCE;
                if(result == null)
                    result = INSTANCE = new DefaultSecurityProtocols();
            }
        return result;
    }
}
