package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mq.MQConnectorConfigurationDescriptor;
import com.google.common.base.Strings;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class JMSDataAcceptorFactory implements DataAcceptorFactory {
    private static final String ACTIVEMQ_PREFIX = "activemq:";

    private static ActiveMQConnectionFactory createActiveMQConnectionFactory(final String connectionString){
        return new ActiveMQConnectionFactory(connectionString);
    }

    @Override
    public DataAcceptor create(final String resourceName,
                               String connectionString,
                               final Map<String, String> parameters) throws Exception {
        final String scriptFile = MQConnectorConfigurationDescriptor.getConverterScript(parameters);
        final JMSDataConverter converter = Strings.isNullOrEmpty(scriptFile) ?
                JMSDataConverter.createDefault() :
                JMSDataConverter.loadFrom(new File(scriptFile), getClass().getClassLoader());
        final ConnectionFactory connectionFactory;
        if (connectionString.startsWith(ACTIVEMQ_PREFIX))     //ActiveMQ detected
            connectionFactory = createActiveMQConnectionFactory(connectionString.replaceFirst(ACTIVEMQ_PREFIX, ""));
        else throw new IllegalArgumentException("Unknown message queue technology");
        return new JMSDataAcceptor(resourceName, parameters, converter, new MQThreadPoolConfig(parameters, resourceName), connectionFactory);
    }

    /**
     * Determines whether this factory can create Data Acceptor using the specified connection string.
     *
     * @param connectionString Candidate connection string.
     * @return {@literal true}, if this factory can be used to produce a new instance of Data Acceptor
     * using specified connection string; otherwise, {@literal false}.
     */
    @Override
    public boolean canCreateFrom(final String connectionString) {
        return connectionString.startsWith(ACTIVEMQ_PREFIX);
    }
}
