package com.itworks.snamp.connectors.jmx;

import com.itworks.snamp.ExceptionalCallable;
import com.itworks.snamp.internal.Utils;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.itworks.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.*;

/**
 * Represents JMX connector initialization options.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxConnectionOptions extends JMXServiceURL implements JmxConnectionFactory {

    private final String login;
    private final String password;
    private final long watchDogPeriod;

    /**
     * Initializes a new JMX connection parameters.
     * @param connectionString JMX-compliant URL that identifies remote managed bean.
     * @throws MalformedURLException The specified URL is not JMX-compliant.
     */
    @SuppressWarnings("UnusedDeclaration")
    JmxConnectionOptions(final String connectionString) throws MalformedURLException {
        this(connectionString, Collections.<String, String>emptyMap());
    }

    JmxConnectionOptions(final String connectionString, final Map<String, String> options) throws MalformedURLException{
        super(connectionString);
        if(options.containsKey(JMX_LOGIN) && options.containsKey(JMX_PASSWORD)){
            login = options.get(JMX_LOGIN);
            password = options.get(JMX_PASSWORD);
        }
        else login = password = "";
        this.watchDogPeriod = options.containsKey(CONNECTION_CHECK_PERIOD) ?
                Integer.valueOf(options.get(CONNECTION_CHECK_PERIOD)) : 3000L;
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

    /**
     * Creates a new instance of the connection manager.
     * @return A new instance of the connection manager.
     */
    public final JmxConnectionManager createConnectionManager(){
        return new JmxConnectionManager(this, watchDogPeriod);
    }

    /**
     * Creates a new direct connection to JMX endpoint.
     * @return A new connection to JMX endpoint.
     * @throws IOException If the connector client or the
     * connection cannot be made because of a communication problem.
     */
    public final JMXConnector createConnection() throws IOException {
        //this string should be used in OSGI environment. Otherwise, JMX connector
        //will not resolve the JMX registry via JNDI
        return Utils.withContextClassLoader(getClass().getClassLoader(), new ExceptionalCallable<JMXConnector, IOException>() {
            @Override
            public JMXConnector call() throws IOException {
                return JMXConnectorFactory.connect(JmxConnectionOptions.this, getJmxOptions());
            }
        });
    }
}