package com.bytex.snamp.connectors.jmx;

import com.bytex.snamp.ExceptionalCallable;
import com.bytex.snamp.connectors.ManagedResourceConfigurationParser;
import com.bytex.snamp.internal.Utils;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.bytex.snamp.connectors.jmx.JmxConnectorConfigurationDescriptor.*;

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

    /**
     * Initializes a new JMX connection parameters.
     * @param connectionString JMX-compliant URL that identifies remote managed bean.
     * @throws MalformedURLException The specified URL is not JMX-compliant.
     */
    JmxConnectionOptions(final String connectionString) throws MalformedURLException, MalformedObjectNameException {
        this(connectionString, Collections.emptyMap());
    }

    JmxConnectionOptions(final String connectionString, final Map<String, String> options) throws MalformedURLException, MalformedObjectNameException {
        super(connectionString);
        final ManagedResourceConfigurationParser parser = new ManagedResourceConfigurationParser() { };
        if(options.containsKey(JMX_LOGIN) && options.containsKey(JMX_PASSWORD)){
            login = options.get(JMX_LOGIN);
            password = options.get(JMX_PASSWORD);
        }
        else login = password = "";
        this.watchDogPeriod = options.containsKey(CONNECTION_CHECK_PERIOD) ?
                Integer.parseInt(options.get(CONNECTION_CHECK_PERIOD)) : 3000L;
        this.smartMode = parser.isSmartModeEnabled(options);
        this.globalNamespace = getObjectName(options);
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
        return Utils.withContextClassLoader(getClass().getClassLoader(), new ExceptionalCallable<JMXConnector, IOException>() {
            @Override
            public JMXConnector call() throws IOException {
                return JMXConnectorFactory.connect(JmxConnectionOptions.this, getJmxOptions());
            }
        });
    }
}