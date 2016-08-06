package com.bytex.snamp.testing.connectors.mq;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.connectors.mq.jms.QueueClient;
import com.bytex.snamp.testing.SnampDependencies;
import com.bytex.snamp.testing.SnampFeature;
import com.bytex.snamp.testing.connectors.AbstractResourceConnectorTest;
import org.osgi.framework.BundleContext;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@SnampDependencies(SnampFeature.MQ_CONNECTOR)
public abstract class AbstractMQConnectorTest extends AbstractResourceConnectorTest {
    private Connection jmsConnection;
    private final QueueClient queue;
    private final String connectionString;

    protected AbstractMQConnectorTest(String connectionString,
                                      final Map<String, String> parameters) {
        super("mq", connectionString, parameters);
        if(connectionString.startsWith("activemq:")) {
            connectionString = connectionString.replaceFirst("activemq:", "");
            this.queue = QueueClient.ACTIVEMQ;
        }
        else if(connectionString.startsWith("jndi://")){
            connectionString = connectionString.replaceFirst("jndi://", "");
            this.queue = QueueClient.JNDI;
        }
        else if(connectionString.startsWith("amqp://") || connectionString.startsWith("amqps://"))
            this.queue = QueueClient.AMQP_0_9_1;
        else throw new IllegalArgumentException("Unsupported MQ technology");
        this.connectionString = connectionString;
    }

    protected final Connection getJmsConnection(){
        return jmsConnection;
    }

    @Override
    protected void beforeStartTest(final BundleContext context) throws JMSException {
        jmsConnection = queue.createConnection(connectionString, context);
        jmsConnection.start();
    }

    @Override
    protected void afterCleanupTest(BundleContext context) throws JMSException {
        jmsConnection.close();
        jmsConnection = null;
    }

    protected final <E extends Throwable> void runTest(final Acceptor<Session, E> testBody) throws JMSException, E {
        final Session session = getJmsConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        try{
            testBody.accept(session);
        }
        finally {
            session.close();
        }
    }
}
