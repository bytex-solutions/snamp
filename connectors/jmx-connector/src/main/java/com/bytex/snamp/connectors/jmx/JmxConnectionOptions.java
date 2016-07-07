package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.Box;
import com.bytex.snamp.internal.Utils;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents JMX connector initialization options.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class JmxConnectionOptions extends JMXServiceURL implements JmxConnectionFactory {

    private static final long serialVersionUID = 2313970631036350249L;
    private final String login;
    private final String password;
    private final long watchDogPeriod;
    private final boolean smartMode;
    private final ObjectName globalNamespace;

    JmxConnectionOptions(final String connectionString, final Map<String, String> options) throws MalformedURLException, MalformedObjectNameException {
        super(connectionString);
        final JmxConnectorDescriptionProvider parser = JmxConnectorDescriptionProvider.getInstance();
        final Box<String> userName = new Box<>(), password = new Box<>();
        parser.parseUserNameAndPassword(options, userName, password);
        this.login = userName.get();
        this.password = password.get();
        this.watchDogPeriod = parser.parseWatchDogPeriod(options);
        this.smartMode = parser.isSmartModeEnabled(options);
        this.globalNamespace = parser.parseRootObjectName(options);
    }

    private Map<String, Object> getJmxOptions(){
        final Map<String, Object> jmxOptions = new HashMap<>(3);
        if(login.length() > 0 && password.length() > 0)
            jmxOptions.put(javax.management.remote.JMXConnector.CREDENTIALS, new String[]{
                    login,
                    password
            });
        return jmxOptions;
    }

    ObjectName getGlobalObjectName(){
        return globalNamespace;
    }

    boolean isSmartModeEnabled(){
        return smartMode;
    }

    /**
     * Creates a new instance of the connection manager.
     * @param logger A logger used by watch dog to report about problems.
     * @return A new instance of the connection manager.
     */
    JmxConnectionManager createConnectionManager(final Logger logger){
        return new JmxConnectionManager(this, watchDogPeriod, logger);
    }

    /**s
     * Creates a new direct connection to JMX endpoint.
     * @return A new connection to JMX endpoint.
     * @throws IOException If the connector client or the
     * connection cannot be made because of a communication problem.
     */
    public JMXConnector createConnection() throws IOException {
        //this string should be used in OSGi environment. Otherwise, JMX connector
        //will not resolve the JMX registry via JNDI
        return Utils.withContextClassLoader(getClass().getClassLoader(), (IOExceptionCallable<JMXConnector>) () -> {
            return JMXConnectorFactory.connect(JmxConnectionOptions.this, getJmxOptions()); //do not replace this anonymous class with lambda
        });
    }
}