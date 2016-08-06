package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.mda.DataAcceptorFactory;
import com.bytex.snamp.connectors.mq.MQResourceConnectorDescriptionProvider;
import com.bytex.snamp.internal.Utils;
import static com.google.common.base.Strings.isNullOrEmpty;
import org.osgi.framework.BundleContext;

import javax.jms.ConnectionFactory;
import java.io.File;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class JMSDataAcceptorFactory implements DataAcceptorFactory {
    private static final String ACTIVEMQ_PREFIX = "activemq:";
    private static final String JNDI_PREFIX = "jndi://";
    private static final String AMQP_PREFIX = "amqp:";
    private static final String AMQP_SECURE_PREFIX = "amqps:";

    @Override
    public JMSDataAcceptor create(final String resourceName,
                                   String connectionString,
                                   final Map<String, String> parameters) throws Exception {
        final BundleContext context = Utils.getBundleContextOfObject(this);
        //parse converter
        final String scriptFile = MQResourceConnectorDescriptionProvider.getInstance().getConverterScript(parameters);
        final JMSDataConverter converter = isNullOrEmpty(scriptFile) ?
                JMSDataConverter.createDefault() :
                JMSDataConverter.loadFrom(new File(scriptFile), getClass().getClassLoader());
        //detect connection factory

        final ConnectionFactory connectionFactory;
        //ActiveMQ detected
        if (connectionString.startsWith(ACTIVEMQ_PREFIX))
            connectionFactory = QueueClient.ACTIVEMQ.getConnectionFactory(connectionString.replaceFirst(ACTIVEMQ_PREFIX, ""), context);
            //JNDI
        else if (connectionString.startsWith(JNDI_PREFIX))
            connectionFactory = QueueClient.JNDI.getConnectionFactory(connectionString.replaceFirst(JNDI_PREFIX, ""), context);
            //AMQP
        else if (connectionString.startsWith(AMQP_PREFIX) || connectionString.startsWith(AMQP_SECURE_PREFIX)) {
            final String protocolVersion = MQResourceConnectorDescriptionProvider.getInstance().getAmqpVersion(parameters);
            final QueueClient client;
            if (isNullOrEmpty(protocolVersion))
                client = QueueClient.AMQP_0_9_1;
            else switch (protocolVersion) {
                case "0-8":
                    client = QueueClient.AMQP_0_8;
                    break;
                case "0-9":
                    client = QueueClient.AMQP_0_9;
                    break;
                case "0-9-1":
                    client = QueueClient.AMQP_0_9_1;
                    break;
                case "0-10":
                    client = QueueClient.AMQP_0_10;
                    break;
                default:
                    client = QueueClient.AMQP_0_9_1;
            }
            connectionFactory = client.getConnectionFactory(connectionString, context);
        } else throw new IllegalArgumentException("Unknown message queue technology");
        //setup thread pool
        return new JMSDataAcceptor(resourceName, parameters, converter, connectionFactory);
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
        return connectionString.startsWith(ACTIVEMQ_PREFIX) ||
                connectionString.startsWith(AMQP_SECURE_PREFIX) ||
                connectionString.startsWith(AMQP_PREFIX) ||
                connectionString.startsWith(JNDI_PREFIX);
    }
}
