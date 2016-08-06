package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.connectors.mq.JMSExceptionUtils;
import com.bytex.snamp.core.ServiceHolder;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.qpid.client.AMQConnectionFactory;
import org.apache.qpid.client.AMQSession;
import org.apache.qpid.configuration.ClientProperties;
import org.apache.qpid.url.URLSyntaxException;
import org.osgi.framework.BundleContext;
import org.osgi.service.jndi.JNDIContextManager;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Represents connection factory for different types of queues.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum QueueClient {
    ACTIVEMQ {
        @Override
        ActiveMQConnectionFactory getConnectionFactory(final String connectionString,
                                                       final BundleContext context) {
            return new ActiveMQConnectionFactory(connectionString);
        }
    },
    AMQP_0_10{
        @Override
        AMQConnectionFactory getConnectionFactory(final String connectionString,
                                                  final BundleContext context) throws JMSException {
            return createAMQPConnectionFactory(connectionString, "0-10");
        }
    },
    AMQP_0_8{
        @Override
        AMQConnectionFactory getConnectionFactory(final String connectionString,
                                                  final BundleContext context) throws JMSException {
            return createAMQPConnectionFactory(connectionString, "0-8");
        }
    },
    AMQP_0_9_1{
        @Override
        AMQConnectionFactory getConnectionFactory(final String connectionString,
                                                  final BundleContext context) throws JMSException {
            return createAMQPConnectionFactory(connectionString, "0-9-1");
        }
    },
    AMQP_0_9{
        @Override
        AMQConnectionFactory getConnectionFactory(final String connectionString,
                                                  final BundleContext context) throws JMSException {
            return createAMQPConnectionFactory(connectionString, "0-9");
        }
    },
    JNDI {
        @Override
        ConnectionFactory getConnectionFactory(final String connectionString,
                                               final BundleContext context) throws JMSException {
            final ServiceHolder<JNDIContextManager> jndiContextFactory = ServiceHolder.tryCreate(context, JNDIContextManager.class);
            if (jndiContextFactory != null)
                try {
                    final Context jndiContext = jndiContextFactory.get().newInitialContext();
                    final Object factory = jndiContext.lookup(connectionString);
                    if (factory instanceof ConnectionFactory)
                        return (ConnectionFactory) factory;
                    else
                        throw new JMSException(String.format("Invalid ConnectionFactory at %s. JNDI is not configured properly", connectionString));
                } catch (final NamingException e) {
                    throw JMSExceptionUtils.wrap(e);
                } finally {
                    jndiContextFactory.release(context);
                }
            else return null;
        }
    };

    static {
        //workaround for QPid AMQP library
        //see https://mail-archives.apache.org/mod_mbox/qpid-users/201412.mbox/%3C5493144F.3040900@genome.wustl.edu%3E
        //see https://cwiki.apache.org/confluence/display/qpid/System+Properties
        if (!System.getProperties().containsKey(AMQSession.IMMEDIATE_PREFETCH))
            System.setProperty(AMQSession.IMMEDIATE_PREFETCH, Boolean.toString(true));
        if (!System.getProperties().containsKey(AMQSession.STRICT_AMQP))
            System.setProperty(AMQSession.STRICT_AMQP, Boolean.toString(false));
    }

    /**
     * Gets JMS connection factory.
     * @param connectionString Connection string describes remote Message Queue.
     * @param context Context of the caller bundle.
     * @return JMS connection factory.
     */
    abstract ConnectionFactory getConnectionFactory(final String connectionString,
                                                    final BundleContext context) throws JMSException;

    public final Connection createConnection(final String connectionString,
                                             final BundleContext context) throws JMSException {
        return getConnectionFactory(connectionString, context).createConnection();
    }

    public final Connection createConnection(final String connectionString,
                                             final String userName,
                                             final String password,
                                             final BundleContext context) throws JMSException{
        return getConnectionFactory(connectionString, context).createConnection(userName, password);
    }

    private static AMQConnectionFactory createAMQPConnectionFactory(final String connectionString,
                                                                    final String protocolVersion) throws JMSException {
        System.setProperty(ClientProperties.AMQP_VERSION, protocolVersion);
        try {
            return new AMQConnectionFactory(connectionString);
        } catch (final URLSyntaxException e) {
            throw JMSExceptionUtils.wrap(e);
        }
    }
}
