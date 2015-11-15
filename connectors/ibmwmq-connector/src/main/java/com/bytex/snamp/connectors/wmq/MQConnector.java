package com.bytex.snamp.connectors.wmq;

import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.ImmutableMap;
import com.ibm.mq.MQException;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.connectors.ManagedResourceConnectorBean;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Connector class for integration with WebSphere MQ.
 * @author  Chernovsky Oleg, Sakno Roman
 * @since 1.1.0
 */
final class MQConnector extends ManagedResourceConnectorBean implements CMQC, CMQCFC {
    public static final class MQDiscoveryService extends BeanDiscoveryService{
        public MQDiscoveryService() throws IntrospectionException {
            super(MQConnector.class);
        }

        /**
         * Gets logger associated with this service.
         *
         * @return The logger associated with this service.
         */
        @Override
        public Logger getLogger() {
            return getLoggerImpl();
        }
    }

    private final PCFMessageAgent mqmonitor;
    private final DateFormat mqDateTimeFormatter;
    private final DiffLongAccumulator bytesSentLastHour;
    private final DiffLongAccumulator bytesSentLast24Hours;
    private final DiffLongAccumulator bytesReceivedLastHour;
    private final DiffLongAccumulator bytesReceivedLast24Hours;
    private final DiffIntAccumulator messagesProcessedLastHour;
    private final DiffIntAccumulator messagesProcessedLast24Hours;
    private final String queueName;
    private final String channelName;


    private MQConnector(final String resourceName,
                        final MQConnectionProperties properties) throws IntrospectionException, MQException {
        super(resourceName);
        mqDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
        queueName = properties.getQueueName();
        channelName = properties.getChannelName();
        mqmonitor = properties.createMessageAgent();
        bytesSentLast24Hours = new DiffLongAccumulator(TimeSpan.ofDays(1));
        bytesSentLastHour = new DiffLongAccumulator(TimeSpan.ofHours(1));
        bytesReceivedLast24Hours = new DiffLongAccumulator(TimeSpan.ofHours(24));
        bytesReceivedLastHour = new DiffLongAccumulator(TimeSpan.ofHours(1));
        messagesProcessedLast24Hours = new DiffIntAccumulator(TimeSpan.ofHours(24));
        messagesProcessedLastHour = new DiffIntAccumulator(TimeSpan.ofHours(1));
    }

    MQConnector(final String resourceName,
                       final String connectionString,
                       final Map<String, String> connectionProperties) throws IntrospectionException, MQException {
        this(resourceName, new MQConnectionProperties(URI.create(connectionString), connectionProperties));
    }

    private PCFMessage sendPcfMessage(final int command, final Map<Integer, ?> filter) throws IOException, MQException{
        final PCFMessage message = new PCFMessage(command);
        for(final Map.Entry<Integer, ?> messageParam: filter.entrySet())
            if(messageParam.getValue() instanceof String)
                message.addParameter(messageParam.getKey(), (String)messageParam.getValue());
            else if(messageParam.getValue() instanceof Long)
                message.addParameter(messageParam.getKey(), (Long)messageParam.getValue());
            else if(messageParam.getValue() instanceof Integer)
                message.addParameter(messageParam.getKey(), (Integer)messageParam.getValue());
            else if(messageParam.getValue() instanceof byte[])
                message.addParameter(messageParam.getKey(), (byte[])messageParam.getValue());
            else if(messageParam.getValue() instanceof int[])
                message.addParameter(messageParam.getKey(), (int[])messageParam.getValue());
        final PCFMessage[] response = mqmonitor.send(message);
        return response.length > 0 ? response[0] : null;
    }

    private String getParameterThroughPCF(final int command,
                                          final Map<Integer, ?> filter,
                                          final int parameter,
                                          final String defaultValue) throws IOException, MQException {
        final PCFMessage response = sendPcfMessage(command, filter);
        final Object value = response.getParameterValue(parameter);
        return Objects.toString(value, defaultValue);
    }

    private long getParameterThroughPCF(final int command,
                                        final Map<Integer, ?> filter,
                                        final int parameter,
                                        final long defaultValue) throws MQException, IOException{
        final PCFMessage response = sendPcfMessage(command, filter);
        final Object value = response.getParameterValue(parameter);
        if(value instanceof String)
            return Long.parseLong((String) value);
        else if(value instanceof Number)
            return ((Number)value).longValue();
        else return defaultValue;
    }

    private int getParameterThroughPCF(final int command,
                                       final Map<Integer, ?> filter,
                                       final int parameter,
                                       final int defaultValue) throws MQException, IOException{
        final PCFMessage response = sendPcfMessage(command, filter);
        final Object value = response.getParameterValue(parameter);
        if(value instanceof String)
            return Integer.parseInt((String)value);
        else if(value instanceof Number)
            return ((Number)value).intValue();
        else return defaultValue;
    }

    private String getQueueParameterThroughPCF(final int parameter, final String defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = ImmutableMap.of(
                MQCA_Q_NAME, queueName,
                MQIACF_Q_STATUS_TYPE, MQIACF_Q_STATUS,
                MQIACF_Q_STATUS_ATTRS, new int[]{parameter});
        return getParameterThroughPCF(MQCMD_INQUIRE_Q_STATUS, filter, parameter, defaultValue);
    }

    private int getQueueParameterThroughPCF(final int parameter, final int defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = ImmutableMap.of(
                MQCA_Q_NAME, queueName,
                MQIACF_Q_STATUS_TYPE, MQIACF_Q_STATUS,
                MQIACF_Q_STATUS_ATTRS, new int[]{parameter});
        return getParameterThroughPCF(MQCMD_INQUIRE_Q_STATUS, filter, parameter, defaultValue);
    }

    private int getChannelParameterThroughPCF(final int parameter, final int defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = ImmutableMap.of(
                MQCACH_CHANNEL_NAME, channelName,
                MQIACH_CHANNEL_INSTANCE_ATTRS, new int[]{parameter});
        return getParameterThroughPCF(MQCMD_INQUIRE_CHANNEL_STATUS, filter, parameter, defaultValue);
    }

    private long getChannelParameterThroughPCF(final int parameter, final long defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = ImmutableMap.of(
                MQCACH_CHANNEL_NAME, channelName,
                MQIACH_CHANNEL_INSTANCE_ATTRS, new int[]{parameter});
        return getParameterThroughPCF(MQCMD_INQUIRE_CHANNEL_STATUS, filter, parameter, defaultValue);
    }

    /**
     * Determines whether the MQ is available.
     * @return {@literal true}, if MQ is available; otherwise, {@literal false}.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Determines whether the MQ is available.")
    public boolean isActive() throws IOException, MQException {
        return getQueueParameterThroughPCF(MQCA_Q_NAME, null) != null;
    }

    /**
     * Gets a time at which the last message was destructively read from the queue.
     * @return A time at which the last message was destructively read from the queue.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     * @throws ParseException Unable to parse date/time.
     */
    @ManagementAttribute(description = "Gets a time at which the last message was destructively read from the queue.")
    public Date getLastGetDate() throws MQException, IOException, ParseException {
        final Map<Integer, ?> filter  = ImmutableMap.of(
                MQCA_Q_NAME, queueName,
                MQIACF_Q_STATUS_TYPE, MQIACF_Q_STATUS,
                MQIACF_Q_STATUS_ATTRS, new int[]{MQCACF_LAST_GET_TIME, MQCACF_LAST_GET_DATE});
        final PCFMessage response = sendPcfMessage(MQCMD_INQUIRE_Q_STATUS, filter);
        //obtain last MQ GET time and date
        final String time = response.getStringParameterValue(MQCACF_LAST_GET_TIME);
        final String date = response.getStringParameterValue(MQCACF_LAST_GET_DATE);
        //create formatter
        return mqDateTimeFormatter.parse(String.format("%s %s", date, time));
    }

    /**
     * Gets a time at which the last message was successfully put to the queue.
     * @return A time at which the last message was successfully put to the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     * @throws ParseException Unable to parse date/time.
     */
    @ManagementAttribute(description = "Gets a time at which the last message was successfully put to the queue.")
    public Date getLastPutDate() throws MQException, IOException, ParseException {
        final Map<Integer, ?> filter  = ImmutableMap.of(
                MQCA_Q_NAME, queueName,
                MQIACF_Q_STATUS_TYPE, MQIACF_Q_STATUS,
                MQIACF_Q_STATUS_ATTRS, new int[]{MQCACF_LAST_PUT_TIME, MQCACF_LAST_PUT_DATE});
        final PCFMessage response = sendPcfMessage(MQCMD_INQUIRE_Q_STATUS, filter);
        //obtain last MQ PUT time and date
        final String time = response.getStringParameterValue(MQCACF_LAST_GET_TIME);
        final String date = response.getStringParameterValue(MQCACF_LAST_GET_DATE);
        //create formatter
        return mqDateTimeFormatter.parse(String.format("%s %s", date, time));
    }

    /**
     * Gets the number of handles that are currently valid for
     * removing messages from the queue by means of the MQGET call.
     * @return The number of handles that are currently valid for removing messages
     * from the queue by means of the MQGET call.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of handles that are currently valid for " +
            "removing messages from the queue by means of the MQGET call.")
    public int getOpenHandlesForInput() throws MQException, IOException{
        return getQueueParameterThroughPCF(MQIA_OPEN_INPUT_COUNT, 0);
    }

    /**
     * Gets the number of handles that are currently valid for adding messages to the queue by means of the MQPUT call.
     * @return The number of handles that are currently valid for adding messages to the queue by means of the MQPUT call
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     * @see <a href='http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/index.jsp?topic=%2Fcom.ibm.mq.csqzaw.doc%2Fuj19050_.htm'>MQIA_OPEN_OUTPUT_COUNT</a>
     */
    @ManagementAttribute(description = "Gets the number of handles that are currently valid for adding messages to the queue by means of the MQPUT call.")
    public int getOpenHandlesForOutput() throws MQException, IOException {
        return getQueueParameterThroughPCF(MQIA_OPEN_OUTPUT_COUNT, 0);
    }

    /**
     * Gets the number of messages currently on the queue.
     * @return The number of messages currently on the queue.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of messages currently on the queue.")
    public int getQueueDepth() throws MQException, IOException {
        return getQueueParameterThroughPCF(MQIACF_Q_STATUS, 0);
    }

    /**
     * Gets age, in seconds, of the oldest message on the queue.
     * @return Age, in seconds, of the oldest message on the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets age, in seconds, of the oldest message on the queue.")
    public int getOldestMessageAge() throws MQException, IOException {
        return getQueueParameterThroughPCF(MQIACF_OLDEST_MSG_AGE, 0);
    }

    /**
     * Gets amount of time, in microseconds, that a message spent on the queue
     * @return  Amount of time, in microseconds, that a message spent on the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets amount of time, in microseconds, that a message spent on the queue")
    public int getMessageOnQueueTime() throws MQException, IOException{
        return getQueueParameterThroughPCF(MQIACF_Q_TIME_INDICATOR, 0);
    }

    /**
     * Gets the number of uncommitted changes (puts and gets) pending for the queue.
     * @return The number of uncommitted changes (puts and gets) pending for the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of uncommitted changes (puts and gets) pending for the queue.")
    public int getUncommittedMessagesCount() throws MQException, IOException{
        return getQueueParameterThroughPCF(MQIACF_UNCOMMITTED_MSGS, 0);
    }

    /**
     * Gets the number of bytes sent through the channel.
     * @return The number of bytes sent through the channel.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of bytes sent through the channel.")
    public long getTotalBytesSent() throws MQException, IOException {
        return getChannelParameterThroughPCF(MQIACH_BYTES_SENT, 0L);
    }

    /**
     * Gets the number of bytes sent through the channel for the last hour.
     * @return The number of bytes sent through the channel for the last hour.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of bytes sent through the channel for the last hour.")
    public long getBytesSentLastHour() throws MQException, IOException {
        return bytesSentLastHour.update(getTotalBytesSent());
    }

    /**
     * Gets the number of bytes sent through the channel for the last day.
     * @return The number of bytes sent through the channel for the last day.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of bytes sent through the channel for the last day.")
    public long getBytesSentLast24Hours() throws MQException, IOException {
        return bytesSentLast24Hours.update(getTotalBytesSent());
    }

    /**
     * Gets the number of bytes received through the channel.
     * @return The number of bytes received through the channel.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of bytes received through the channel.")
    public long getTotalBytesReceived() throws MQException, IOException {
        return getChannelParameterThroughPCF(MQIACH_BYTES_RECEIVED, 0L);
    }

    /**
     * Gets the number of bytes received through the channel for the last hour.
     * @return The number of bytes received through the channel for the last hour.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of bytes received through the channel for the last hour.")
    public long getBytesReceivedLastHour() throws MQException, IOException{
        return bytesReceivedLastHour.update(getTotalBytesReceived());
    }

    /**
     * Gets the number of bytes received through the channel for the last day.
     * @return The number of bytes received through the channel for the last hour.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of bytes received through the channel for the last day.")
    public long getBytesReceivedLast24Hours() throws MQException, IOException{
        return bytesReceivedLast24Hours.update(getTotalBytesReceived());
    }

    /**
     * Gets the number of messages sent or received, or number of MQI calls handled.
     * @return The number of messages sent or received, or number of MQI calls handled
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of messages sent or received, or number of MQI calls handled.")
    public int getProcessedMessagesCount() throws MQException, IOException {
        return getChannelParameterThroughPCF(MQIACH_MSGS, 0);
    }

    /**
     * Gets the number of messages sent or received, or number of MQI calls handled for the last hour.
     * @return The number of messages sent or received, or number of MQI calls handled for the last hour.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of messages sent or received, or number of MQI calls handled for the last hour.")
    public int getProcessedMessagesCountLastHour() throws MQException, IOException {
        return messagesProcessedLastHour.update(getProcessedMessagesCount());
    }

    /**
     * Gets the number of messages sent or received, or number of MQI calls handled for the last day.
     * @return The number of messages sent or received, or number of MQI calls handled for the last day.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute(description = "Gets the number of messages sent or received, or number of MQI calls handled for the last day.")
    public int getProcessedMessagesCountLast24Hours() throws MQException, IOException {
        return messagesProcessedLast24Hours.update(getProcessedMessagesCount());
    }

    /**
     * Determines whether raising of registered events is suspended.
     *
     * @return {@literal true}, if events are suspended; otherwise {@literal false}.
     */
    @Override
    public boolean isSuspended() {
        return super.isSuspended() && !DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this));
    }

    static Logger getLoggerImpl(){
        return getLogger(getConnectorType(MQConnector.class));
    }

    @Override
    public void close() throws Exception {
        try {
            mqmonitor.disconnect();
        } finally {
            super.close();
        }
    }
}
