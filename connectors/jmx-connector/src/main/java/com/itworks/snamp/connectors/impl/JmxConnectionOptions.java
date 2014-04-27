package com.itworks.snamp.connectors.impl;

import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents JMX connector initialization options.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectionOptions extends JMXServiceURL {
    private static final String LOGIN_OPTION = "login";
    private static final String PASSWORD_OPTION = "password";

    private final String login;
    private final String password;

    /**
     * Initializes a new JMX connection parameters.
     * @param connectionString JMX-compliant URL that identifies remote managed bean.
     * @throws MalformedURLException The specified URL is not JMX-compliant.
     */
    @SuppressWarnings("UnusedDeclaration")
    public JmxConnectionOptions(final String connectionString) throws MalformedURLException {
        this(connectionString, Collections.<String, String>emptyMap());
    }

    public JmxConnectionOptions(final String connectionString, final Map<String, String> options) throws MalformedURLException{
        super(connectionString);
        if(options.containsKey(LOGIN_OPTION) && options.containsKey(PASSWORD_OPTION)){
            login = options.get(LOGIN_OPTION);
            password = options.get(PASSWORD_OPTION);
        }
        else login = password = "";
    }

    /**
     * Creates a new instance of the connection manager.
     * @return A new instance of the connection manager.
     */
    public final JmxConnectionManager createConnectionManager(){
        final Map<String, Object> jmxOptions = new HashMap<>(3);
        if(login.length() > 0 && password.length() > 0)
            jmxOptions.put(javax.management.remote.JMXConnector.CREDENTIALS, new String[]{
                    login,
                    password
            });
        return new JmxConnectionManager(this, jmxOptions);
    }
}
