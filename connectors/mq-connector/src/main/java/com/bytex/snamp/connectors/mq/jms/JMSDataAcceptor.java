package com.bytex.snamp.connectors.mq.jms;

import com.bytex.snamp.configuration.AbsentConfigurationParameterException;
import com.bytex.snamp.connectors.mda.DataAcceptor;
import com.bytex.snamp.connectors.mda.MDAAttributeRepository;
import com.bytex.snamp.connectors.mda.MDANotificationRepository;
import com.bytex.snamp.connectors.mq.MQResourceConnectorConfigurationDescriptor;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;

import javax.jms.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JMSDataAcceptor extends DataAcceptor implements ExceptionListener {
    private final Connection jmsConnection;
    private Session jmsSession;
    private final String queueName;
    private final boolean isTopic;
    private final String messageSelector;
    private final JMSAttributeRepository attributes;
    private final JMSNotificationRepository notifications;
    private final String outputQueueName;
    private final boolean isTopicOutput;
    private final JMSDataConverter dataConverter;

    JMSDataAcceptor(final String resourceName,
                    final Map<String, String> parameters,
                    final JMSDataConverter converter,
                    final Supplier<ExecutorService> threadPoolFactory,
                    final ConnectionFactory factory) throws JMSException, AbsentConfigurationParameterException {
        dataConverter = converter;
        jmsConnection = MQResourceConnectorConfigurationDescriptor.createConnection(factory, parameters);
        queueName = MQResourceConnectorConfigurationDescriptor.getInputQueueName(parameters);
        isTopic = MQResourceConnectorConfigurationDescriptor.isInputTopic(parameters);
        messageSelector = MQResourceConnectorConfigurationDescriptor.getMessageSelector(parameters);
        outputQueueName = MQResourceConnectorConfigurationDescriptor.getOutputQueueName(parameters);
        isTopicOutput = MQResourceConnectorConfigurationDescriptor.isOutputTopic(parameters);
        attributes = new JMSAttributeRepository(resourceName, converter, getLogger());
        notifications = new JMSNotificationRepository(resourceName, threadPoolFactory.get(), converter, getLogger());
    }

    /**
     * Gets repository of attributes provided by this connector.
     *
     * @return Repository of attributes.
     */
    @Override
    protected MDAAttributeRepository<?> getAttributes() {
        return attributes;
    }

    /**
     * Gets repository of notifications metadata provided by this connector.
     *
     * @return Repository of notifications metadata.
     */
    @Override
    protected MDANotificationRepository getNotifications() {
        return notifications;
    }

    /**
     * Starts listening of incoming monitoring data.
     *
     * @param dependencies List of connector dependencies.
     * @throws IOException Unable to start listening data.
     */
    @Override
    public void beginListening(final Object... dependencies) throws IOException {
        try {
            jmsConnection.setExceptionListener(this);
            jmsConnection.start();
            jmsSession = jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            attributes.init(jmsSession,
                    outputQueueName,
                    isTopicOutput);
            final Destination dest = isTopic ? jmsSession.createTopic(queueName) : jmsSession.createQueue(queueName);
            final MessageConsumer consumer = Strings.isNullOrEmpty(messageSelector) ?
                    jmsSession.createConsumer(dest) :
                    jmsSession.createConsumer(dest, messageSelector);
            consumer.setMessageListener(new JMSMessageListener(attributes, notifications, dataConverter, getLogger()));
        } catch (final JMSException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            else throw new IOException(e);
        }
    }

    @Override
    public void onException(final JMSException e) {
        getLogger().log(Level.SEVERE, "JMS subsystem error", e);
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
