package com.bytex.snamp.testing.connectors.mq;

import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import com.google.common.collect.ImmutableMap;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.osgi.framework.BundleContext;

import javax.jms.Connection;
import javax.jms.JMSException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.MQ_CONNECTOR)
public abstract class AbstractMQConnectorTest extends AbstractResourceConnectorTest {
    protected enum QueueType{
        ACTIVEMQ("activemq:vm://localhost") {
            @Override
            Connection createConnection() throws JMSException {
                return new ActiveMQConnectionFactory(super.connectionString.replaceFirst("activemq:", "")).createConnection();
            }
        };
        private String connectionString;

        QueueType(final String connectionString){
            this.connectionString = connectionString;
        }

        abstract Connection createConnection() throws JMSException;
    }

    private Connection jmsConnection;
    private final QueueType queue;

    protected AbstractMQConnectorTest(final QueueType queue, final String inputQueueName) {
        super("mq", queue.connectionString, ImmutableMap.of("inputQueueName", inputQueueName));
        this.queue = queue;
    }

    protected AbstractMQConnectorTest(final QueueType queue, final String inputQueueName, final String outputQueueName) {
        super("mq", queue.connectionString, ImmutableMap.of("inputQueueName", inputQueueName, "outputQueueName", outputQueueName));
        this.queue = queue;
    }

    protected final Connection getJmsConnection(){
        return jmsConnection;
    }

    @Override
    protected void beforeStartTest(BundleContext context) throws JMSException {
        jmsConnection = queue.createConnection();
        jmsConnection.start();
    }

    @Override
    protected void afterCleanupTest(BundleContext context) throws JMSException {
        jmsConnection.close();
        jmsConnection = null;
    }
}
