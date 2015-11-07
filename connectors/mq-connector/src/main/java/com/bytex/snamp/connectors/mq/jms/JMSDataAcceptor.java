package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.MDAAttributeRepository;
import com.bytex.snamp.connectors.mda.MDANotificationRepository;
import com.bytex.snamp.connectors.mq.MQConnectorConfigurationDescriptor;
import com.google.common.base.Function;
import com.google.common.base.Strings;

import javax.jms.*;
import java.io.IOException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JMSDataAcceptor extends DataAcceptor {
    private final Connection jmsConnection;
    private Session jmsSession;
    private final String queueName;
    private final boolean isTopic;
    private final String messageSelector;

    JMSDataAcceptor(final String resourceName,
                    final Map<String, String> parameters,
                    final ConnectionFactory factory) throws JMSException, AbsentConfigurationParameterException {
        jmsConnection = MQConnectorConfigurationDescriptor.createConnection(factory, parameters);
        queueName = MQConnectorConfigurationDescriptor.getQueueName(parameters);
        isTopic = MQConnectorConfigurationDescriptor.isTopic(parameters);
        messageSelector = MQConnectorConfigurationDescriptor.getMessageSelector(parameters);
    }

    /**
     * Gets repository of attributes provided by this connector.
     *
     * @return Repository of attributes.
     */
    @Override
    protected MDAAttributeRepository<?> getAttributes() {
        return null;
    }

    /**
     * Gets repository of notifications metadata provided by this connector.
     *
     * @return Repository of notifications metadata.
     */
    @Override
    protected MDANotificationRepository getNotifications() {
        return null;
    }

    /**
     * Starts listening of incoming monitoring data.
     *
     * @param dependencies List of connector dependencies.
     * @throws IOException Unable to start listening data.
     */
    @Override
    public void beginListening(Object... dependencies) throws IOException {
        try {
            jmsConnection.start();
            jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            final Destination dest = isTopic ? jmsSession.createQueue(queueName) : jmsSession.createTopic(queueName);
            final MessageConsumer consumer = Strings.isNullOrEmpty(messageSelector) ?
                    jmsSession.createConsumer(dest) :
                    jmsSession.createConsumer(dest, messageSelector);
        } catch (final JMSException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            else throw new IOException(e);
        }
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the aggregated object.
     * @return An instance of the requested object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return findObject(objectType, new Function<Class<T>, T>() {
            @Override
            public T apply(final Class<T> objectType) {
                return JMSDataAcceptor.super.queryObject(objectType);
            }
        }, jmsConnection);
    }

    /**
     * Releases all resources associated with this connector.
     *
     * @throws Exception Unable to release resources associated with this connector.
     */
    @Override
    public void close() throws Exception {
        if(jmsSession != null)
            jmsSession.close();
        jmsSession = null;
        jmsConnection.close();
        super.close();
    }
}
