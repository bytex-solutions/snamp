package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
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
                               final Map<String, String> parameters) throws JMSException, AbsentConfigurationParameterException {
        final ConnectionFactory connectionFactory;
        if (connectionString.startsWith(ACTIVEMQ_PREFIX))     //ActiveMQ detected
            connectionFactory = createActiveMQConnectionFactory(connectionString.replaceFirst(ACTIVEMQ_PREFIX, ""));
        else throw new IllegalArgumentException("Unknown message queue technology");
        return new JMSDataAcceptor(resourceName, parameters, new MQThreadPoolConfig(parameters, resourceName), connectionFactory);
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
