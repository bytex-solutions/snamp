package com.snamp.connectors;

import static com.snamp.Pair.*;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;
import com.snamp.TimeSpan;
import com.snamp.internal.TimeBasedCache;
import static com.snamp.internal.TimeBasedCache.CacheSupplier;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.URI;
import java.text.*;
import java.util.*;
import java.util.logging.*;

/**
 * Connector class for integration with WebSphere MQ.
 * @author  Chernovsky Oleg, Sakno Roman
 * @since 1.1.0
 */
final class IbmWmqConnector extends ManagementConnectorBean {
    public static final String NAME = IbmWmqHelpers.CONNECTOR_NAME;
    private final PCFMessageAgent mqmonitor;
    private final Logger log;
    private final MQConnectionProperties properties;
    private static final DateFormat MQ_DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    private final TimeBasedCache<Long> bytesSentLastHour;
    private final TimeBasedCache<Long> bytesSentLast24Hours;
    private final TimeBasedCache<Long> bytesReceivedLastHour;
    private final TimeBasedCache<Long> bytesReceivedLast24Hours;
    private final TimeBasedCache<Integer> messagesProcessedLastHour;
    private final TimeBasedCache<Integer> messagesProcessedLast24Hours;

    private IbmWmqConnector(final MQConnectionProperties connectionString) throws IntrospectionException, MQException{
        super(new IbmWmqTypeSystem());
        log = IbmWmqHelpers.getLogger();
        this.properties = connectionString;
        this.mqmonitor = connectionString.createMessageAgent();
        bytesSentLast24Hours = new TimeBasedCache<>(TimeSpan.fromHours(24), 0L);
        bytesSentLast24Hours.start();
        bytesSentLastHour = new TimeBasedCache<>(TimeSpan.fromHours(1), 0L);
        bytesSentLastHour.start();
        bytesReceivedLast24Hours = new TimeBasedCache<>(TimeSpan.fromHours(24), 0L);
        bytesReceivedLast24Hours.start();
        bytesReceivedLastHour = new TimeBasedCache<>(TimeSpan.fromHours(1), 0L);
        bytesReceivedLastHour.start();
        messagesProcessedLastHour = new TimeBasedCache<>(TimeSpan.fromHours(1), 0);
        messagesProcessedLastHour.start();
        messagesProcessedLast24Hours = new TimeBasedCache<>(TimeSpan.fromHours(24), 0);
        messagesProcessedLast24Hours.start();
    }

    /**
     * Initializes a new management connector.
     *
     * @param  connectionString string with address of the MQ and channel
     * @throws IllegalArgumentException
     *          typeBuilder is {@literal null}.
     */
    public IbmWmqConnector(final String connectionString, final Map<String, String> connectionProperties) throws IntrospectionException, MQException {
        this(new MQConnectionProperties(URI.create(connectionString), connectionProperties));
    }

    private PCFMessage sendPcfMessage(final int command, final Map<Integer, ?> filter) throws IOException, MQException{
        final PCFMessage message = new PCFMessage(command);
        for(final int messageParam: filter.keySet()){
            final Object messageParamValue = filter.get(messageParam);
            if(messageParamValue instanceof String)
                message.addParameter(messageParam, (String)messageParamValue);
            else if(messageParamValue instanceof Long)
                message.addParameter(messageParam, (Long)messageParamValue);
            else if(messageParamValue instanceof Integer)
                message.addParameter(messageParam, (Integer)messageParamValue);
            else if(messageParamValue instanceof byte[])
                message.addParameter(messageParam, (byte[])messageParamValue);
            else if(messageParamValue instanceof int[])
                message.addParameter(messageParam, (int[])messageParamValue);
            else continue;
        }
        final PCFMessage[] response = mqmonitor.send(message);
        return response.length > 0 ? response[0] : null;
    }

    private String getParameterThroughPCF(final int command, final Map<Integer, ?> filter, final int parameter, final String defaultValue) throws IOException, MQException {
        final PCFMessage response = sendPcfMessage(command, filter);
        final Object value = response.getParameterValue(parameter);
        return Objects.toString(value, "");
    }

    private long getParameterThroughPCF(final int command, final Map<Integer, ?> filter, final int parameter, final long defaultValue) throws MQException, IOException{
        final PCFMessage response = sendPcfMessage(command, filter);
        final Object value = response.getParameterValue(parameter);
        if(value instanceof String)
            return Long.valueOf((String)value);
        else if(value instanceof Number)
            return ((Number)value).longValue();
        else return defaultValue;
    }

    private int getParameterThroughPCF(final int command, final Map<Integer, ?> filter, final int parameter, final int defaultValue) throws MQException, IOException{
        final PCFMessage response = sendPcfMessage(command, filter);
        final Object value = response.getParameterValue(parameter);
        if(value instanceof String)
            return Integer.valueOf((String)value);
        else if(value instanceof Number)
            return ((Number)value).intValue();
        else return defaultValue;
    }

    private String getQueueParameterThroughPCF(final int parameter, final String defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQC.MQCA_Q_NAME, properties.getQueueName()),
                pair(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS),
                pair(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[]{parameter}));
        return getParameterThroughPCF(CMQCFC.MQCMD_INQUIRE_Q_STATUS, filter, parameter, defaultValue);
    }

    private int getQueueParameterThroughPCF(final int parameter, final int defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQC.MQCA_Q_NAME, properties.getQueueName()),
                pair(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS),
                pair(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[]{parameter}));
        return getParameterThroughPCF(CMQCFC.MQCMD_INQUIRE_Q_STATUS, filter, parameter, defaultValue);
    }

    private long getQueueParameterThroughPCF(final int parameter, final long defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQC.MQCA_Q_NAME, properties.getQueueName()),
                pair(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS),
                pair(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[]{parameter}));
        return getParameterThroughPCF(CMQCFC.MQCMD_INQUIRE_Q_STATUS, filter, parameter, defaultValue);
    }

    private String getChannelParameterThroughPCF(final int parameter, final String defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQCFC.MQCACH_CHANNEL_NAME, properties.getChannelName()),
                pair(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[]{parameter}));
        return getParameterThroughPCF(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS, filter, parameter, defaultValue);
    }

    private int getChannelParameterThroughPCF(final int parameter, final int defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQCFC.MQCACH_CHANNEL_NAME, properties.getChannelName()),
                pair(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[]{parameter}));
        return getParameterThroughPCF(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS, filter, parameter, defaultValue);
    }

    private long getChannelParameterThroughPCF(final int parameter, final long defaultValue) throws IOException, MQException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQCFC.MQCACH_CHANNEL_NAME, properties.getChannelName()),
                pair(CMQCFC.MQIACH_CHANNEL_INSTANCE_ATTRS, new int[]{parameter}));
        return getParameterThroughPCF(CMQCFC.MQCMD_INQUIRE_CHANNEL_STATUS, filter, parameter, defaultValue);
    }

    /**
     * Determines whether the MQ is available.
     * @return {@literal true}, if MQ is available; otherwise, {@literal false}.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public boolean isActive() {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQC.MQCA_Q_NAME, properties.getQueueName()),
                pair(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS),
                pair(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[]{CMQC.MQCA_Q_NAME}));
        try {
            return sendPcfMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS, filter) != null;
        } catch (final IOException | MQException e) {
            log.log(Level.WARNING, "Connection with MQ was lost.", e);
            return false;
        }
    }

    /**
     * Gets a time at which the last message was destructively read from the queue.
     * @return A time at which the last message was destructively read from the queue.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     * @throws ParseException Unable to parse date/time.
     */
    @ManagementAttribute
    public Date getLastGetDate() throws MQException, IOException, ParseException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQC.MQCA_Q_NAME, properties.getQueueName()),
                pair(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS),
                pair(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[]{CMQCFC.MQCACF_LAST_GET_TIME, CMQCFC.MQCACF_LAST_GET_DATE}));
        final PCFMessage response = sendPcfMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS, filter);
        //obtain last MQ GET time and date
        final String time = response.getStringParameterValue(CMQCFC.MQCACF_LAST_GET_TIME);
        final String date = response.getStringParameterValue(CMQCFC.MQCACF_LAST_GET_DATE);
        //create formatter
        return MQ_DATE_TIME_FORMATTER.parse(String.format("%s %s", date, time));
    }

    /**
     * Gets a time at which the last message was successfully put to the queue.
     * @return A time at which the last message was successfully put to the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     * @throws ParseException Unable to parse date/time.
     */
    @ManagementAttribute
    public Date getLastPutDate() throws MQException, IOException, ParseException {
        final Map<Integer, ?> filter  = toMap(
                pair(CMQC.MQCA_Q_NAME, properties.getQueueName()),
                pair(CMQCFC.MQIACF_Q_STATUS_TYPE, CMQCFC.MQIACF_Q_STATUS),
                pair(CMQCFC.MQIACF_Q_STATUS_ATTRS, new int[]{CMQCFC.MQCACF_LAST_PUT_TIME, CMQCFC.MQCACF_LAST_PUT_DATE}));
        final PCFMessage response = sendPcfMessage(CMQCFC.MQCMD_INQUIRE_Q_STATUS, filter);
        //obtain last MQ PUT time and date
        final String time = response.getStringParameterValue(CMQCFC.MQCACF_LAST_GET_TIME);
        final String date = response.getStringParameterValue(CMQCFC.MQCACF_LAST_GET_DATE);
        //create formatter
        return MQ_DATE_TIME_FORMATTER.parse(String.format("%s %s", date, time));
    }

    /**
     * Gets the number of handles that are currently valid for
     * removing messages from the queue by means of the MQGET call.
     * @return The number of handles that are currently valid for removing messages
     * from the queue by means of the MQGET call.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public int getOpenHandlesForInput() throws MQException, IOException{
        return getQueueParameterThroughPCF(CMQC.MQIA_OPEN_INPUT_COUNT, 0);
    }

    /**
     * Gets the number of handles that are currently valid for adding messages to the queue by means of the MQPUT call.
     * @return The number of handles that are currently valid for adding messages to the queue by means of the MQPUT call
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     * @see <a href='http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/index.jsp?topic=%2Fcom.ibm.mq.csqzaw.doc%2Fuj19050_.htm'>MQIA_OPEN_OUTPUT_COUNT</a>
     */
    @ManagementAttribute
    public int getOpenHandlesForOutput() throws MQException, IOException {
        return getQueueParameterThroughPCF(CMQC.MQIA_OPEN_OUTPUT_COUNT, 0);
    }

    /**
     * Gets the integer attribute selector is used with an MQINQ call to determine the number of messages
     * currently on the queue.
     * @return
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public int getQueueDepth() throws MQException, IOException {
        return getQueueParameterThroughPCF(CMQCFC.MQIACF_Q_STATUS, 0);
    }

    /**
     * Gets age, in seconds, of the oldest message on the queue.
     * @return Age, in seconds, of the oldest message on the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public int getOldestMessageAge() throws MQException, IOException {
        return getQueueParameterThroughPCF(CMQCFC.MQIACF_OLDEST_MSG_AGE, 0);
    }

    /**
     * Gets amount of time, in microseconds, that a message spent on the queue
     * @return  Amount of time, in microseconds, that a message spent on the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public int getMessageOnQueueTime() throws MQException, IOException{
        return getQueueParameterThroughPCF(CMQCFC.MQIACF_Q_TIME_INDICATOR, 0);
    }

    /**
     * Gets the number of uncommitted changes (puts and gets) pending for the queue.
     * @return The number of uncommitted changes (puts and gets) pending for the queue
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public int getUncommittedMessagesCount() throws MQException, IOException{
        return getQueueParameterThroughPCF(CMQCFC.MQIACF_UNCOMMITTED_MSGS, 0);
    }

    /**
     * Gets the number of bytes sent through the channel.
     * @return The number of bytes sent through the channel.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public long getTotalBytesSent() throws MQException, IOException {
        return getChannelParameterThroughPCF(CMQCFC.MQIACH_BYTES_SENT, 0L);
    }

    /**
     * Gets the number of bytes sent through the channel for the last hour.
     * @return The number of bytes sent through the channel for the last hour.
     * @throws Exception Invalid MQ parameters; or network connection problems.
     */
    @ManagementAttribute
    public long getBytesSentLastHour() throws Exception {
        return bytesSentLastHour.getOrRenewCache(new CacheSupplier<Long, Exception>() {
            @Override
            public Long newCacheValue(final Long previousValue) throws Exception {
                return getTotalBytesSent() - previousValue;
            }
        });
    }

    /**
     * Gets the number of bytes sent through the channel for the last day.
     * @return The number of bytes sent through the channel for the last day.
     * @throws Exception Invalid MQ parameters; or network connection problems.
     */
    @ManagementAttribute
    public long getBytesSentLast24Hours() throws Exception {
        return bytesSentLast24Hours.getOrRenewCache(new CacheSupplier<Long, Exception>() {
            @Override
            public Long newCacheValue(final Long previousValue) throws Exception {
                return getTotalBytesSent() - previousValue;
            }
        });
    }

    /**
     * Gets the number of bytes received through the channel.
     * @return The number of bytes received through the channel.
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public long getTotalBytesReceived() throws MQException, IOException {
        return getChannelParameterThroughPCF(CMQCFC.MQIACH_BYTES_RECEIVED, 0L);
    }

    /**
     * Gets the number of bytes received through the channel for the last hour.
     * @return The number of bytes received through the channel for the last hour.
     * @throws Exception Invalid MQ parameters; or network connection problems.
     */
    @ManagementAttribute
    public long getBytesReceivedLastHour() throws Exception{
        return bytesReceivedLastHour.getOrRenewCache(new CacheSupplier<Long, Exception>() {
            @Override
            public Long newCacheValue(final Long previousValue) throws Exception {
                return getTotalBytesReceived() - previousValue;
            }
        });
    }

    /**
     * Gets the number of bytes received through the channel for the last hour.
     * @return The number of bytes received through the channel for the last hour.
     * @throws Exception Invalid MQ parameters; or network connection problems.
     */
    @ManagementAttribute
    public long getBytesReceivedLast24Hours() throws Exception{
        return bytesReceivedLast24Hours.getOrRenewCache(new CacheSupplier<Long, Exception>() {
            @Override
            public Long newCacheValue(final Long previousValue) throws Exception {
                return getTotalBytesReceived() - previousValue;
            }
        });
    }

    /**
     * Gets the number of messages sent or received, or number of MQI calls handled.
     * @return The number of messages sent or received, or number of MQI calls handled
     * @throws MQException Invalid MQ parameters.
     * @throws IOException Network connection problems.
     */
    @ManagementAttribute
    public int getProcessedMessagesCount() throws MQException, IOException {
        return getChannelParameterThroughPCF(CMQCFC.MQIACH_MSGS, 0);
    }

    /**
     * Gets the number of messages sent or received, or number of MQI calls handled for the last hour.
     * @return The number of messages sent or received, or number of MQI calls handled for the last hour.
     * @throws Exception Exception Invalid MQ parameters; or network connection problems.
     */
    @ManagementAttribute
    public int getProcessedMessagesCountLastHour() throws Exception {
        return messagesProcessedLastHour.getOrRenewCache(new CacheSupplier<Integer, Exception>() {
            @Override
            public Integer newCacheValue(final Integer previousValue) throws Exception {
                return getProcessedMessagesCount() - previousValue;
            }
        });
    }

    /**
     * Gets the number of messages sent or received, or number of MQI calls handled for the last day.
     * @return The number of messages sent or received, or number of MQI calls handled for the last day.
     * @throws Exception Exception Invalid MQ parameters; or network connection problems.
     */
    @ManagementAttribute
    public int getProcessedMessagesCountLast24Hours() throws Exception {
        return messagesProcessedLast24Hours.getOrRenewCache(new CacheSupplier<Integer, Exception>() {
            @Override
            public Integer newCacheValue(final Integer previousValue) throws Exception {
                return getProcessedMessagesCount() - previousValue;
            }
        });
    }

    @Override
    public void close() throws Exception {
        super.close();
        mqmonitor.disconnect();
    }
}
