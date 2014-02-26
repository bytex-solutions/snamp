package com.snamp.connectors;

import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;

import com.snamp.SimpleTable;
import com.snamp.Table;

import java.util.*;

public class IbmWmqTypeSystem extends WellKnownTypeSystem {

    /**
     * Classes for entities status table representation
     * Created in need to distinguish multiple getters that return same types
     *
     */

    final public static class QueueStatusTable extends ArrayList<PCFMessage> {
        public QueueStatusTable(List<PCFMessage> c) {
            super(c);
        }
    }

    final public static class ChannelStatusTable extends ArrayList<PCFMessage> {
        public ChannelStatusTable(Collection<PCFMessage> c) {
            super(c);
        }
    }

    final public static class QMgrStatusTable extends ArrayList<PCFMessage> {
        public QMgrStatusTable(Collection<PCFMessage> c) {
            super(c);
        }
    }

    final public static class ServiceStatusTable extends ArrayList<PCFMessage> {
        public ServiceStatusTable(Collection<PCFMessage> c) {
            super(c);
        }
    }

    @Converter
    public static Table<String> convertToTable(QueueStatusTable queueRows) {
        final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
            put("MQCA_Q_NAME",                  String.class);
            put("MQIACF_Q_STATUS_TYPE",         Integer.class);
            put("MQIA_CURRENT_Q_DEPTH",         Integer.class);
            put("MQIA_OPEN_INPUT_COUNT",        Integer.class);
            put("MQCACF_LAST_GET_DATE",         String.class);
            put("MQCACF_LAST_GET_TIME",         String.class);
            put("MQCACF_LAST_PUT_DATE",         String.class);
            put("MQCACF_LAST_PUT_TIME",         String.class);
            put("MQCACF_MEDIA_LOG_EXTENT_NAME", String.class);
            put("MQIA_MONITORING_Q",            Integer.class);
            put("MQIACF_OLDEST_MSG_AGE",        Integer.class);
            put("MQIA_OPEN_OUTPUT_COUNT",       Integer.class);
            put("MQIACF_Q_TIME_INDICATOR",      String.class);
            put("MQIACF_UNCOMMITTED_MSGS",      Integer.class);
        }};

        final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
        fillTableWithMessages(resTable, queueRows);

        return resTable;
    }

    @Converter
    public static Table<String> convertToTable(ChannelStatusTable channelRows) {
        final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
            put("MQCACH_CHANNEL_NAME",                          String.class);
            put("MQIACH_CHANNEL_TYPE",                          Integer.class);
            put("MQIACH_BUFFERS_RCVD/MQIACH_BUFFERS_RECEIVED",  Integer.class);
            put("MQIACH_BUFFERS_SENT",                          Integer.class);
            put("MQIACH_BYTES_RCVD/MQIACH_BYTES_RECEIVED",      Integer.class);
            put("MQIACH_BYTES_SENT",                            Integer.class);
            put("MQCACH_CHANNEL_START_DATE",                    String.class);
            put("MQCACH_CHANNEL_START_TIME",                    String.class);
            put("MQIACH_HDR_COMPRESSION",                       String.class);
            put("MQIACH_MSG_COMPRESSION",                       String.class);
            put("MQIACH_COMPRESSION_RATE",                      String.class);
            put("MQIACH_COMPRESSION_TIME",                      String.class);
            put("MQCACH_CONNECTION_NAME",                       String.class);
            put("MQIACH_CHANNEL_INSTANCE_TYPE",                 Integer.class);
            put("MQIACH_EXIT_TIME_INDICATOR",                   String.class);
            put("MQIACH_HB_INTERVAL",                           Integer.class);

            put("MQCACH_MCA_JOB_NAME",                          String.class);
            put("MQCACH_LOCAL_ADDRESS",                         String.class);
            put("MQCACH_LAST_MSG_DATE",                         String.class);
            put("MQCACH_LAST_MSG_TIME",                         String.class);
            put("MQIACH_MCA_STATUS",                            Integer.class);
            put("MQCACH_MCA_USER_ID",                           String.class);
            put("MQIA_MONITORING_CHANNEL",                      Integer.class);
            put("MQIACH_MSGS",                                  Integer.class);
            put("MQCACH_REMOTE_APPL_TAG",                       String.class);
            put("MQCACH_SSL_CERT_ISSUER_NAME",                  String.class);
            put("MQCACH_SSL_KEY_RESET_DATE",                    String.class);
            put("MQCACH_SSL_KEY_RESET_TIME",                    String.class);
            put("MQCACH_SSL_SHORT_PEER_NAME",                   String.class);
            put("MQIACH_SSL_KEY_RESETS",                        Integer.class);
            put("MQIACH_CHANNEL_STATUS",                        Integer.class);
            put("MQIACH_STOP_REQUESTED",                        Integer.class);

            put("MQIACH_CHANNEL_SUBSTATE",                      Integer.class);
            put("MQIACH_CURRENT_SHARING_CONVS",                 Integer.class);
            put("MQIACH_MAX_SHARING_CONVS",                     Integer.class);
            put("MQCACH_REMOTE_VERSION",                        String.class);
            put("MQCACH_REMOTE_PRODUCT",                        String.class);
        }};

        final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
        fillTableWithMessages(resTable, channelRows);

        return resTable;
    }

    @Converter
    public static Table<String> convertToTable(QMgrStatusTable qmgrRows) {
        final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
            put("MQCA_INSTALLATION_DESC",           String.class);
            put("MQCACF_LOG_PATH",                  String.class);
            put("MQCACF_Q_MGR_START_DATE",          String.class);
            put("MQCACF_Q_MGR_START_TIME",          String.class);
            put("MQCA_INSTALLATION_PATH",           String.class);
            put("MQIACF_Q_MGR_STATUS",              Integer.class);
            put("MQCA_Q_MGR_NAME",                  String.class);
            put("MQCACF_RESTART_LOG_EXTENT_NAME",   String.class);
            put("MQCA_INSTALLATION_NAME",           String.class);
            put("MQCACF_CURRENT_LOG_EXTENT_NAME",   String.class);
            put("MQIACF_CONNECTION_COUNT",          Integer.class);
            put("MQIACF_CHINIT_STATUS",             Integer.class);
            put("MQIACF_PERMIT_STANDBY",            Integer.class);
            put("MQCACF_MEDIA_LOG_EXTENT_NAME",     String.class);
            put("MQIACF_CMD_SERVER_STATUS",         Integer.class);
        }};

        final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
        fillTableWithMessages(resTable, qmgrRows);

        return resTable;
    }

    @Converter
    public static Table<String> convertToTable(ServiceStatusTable serviceRows) {
        final Map<String, Class<?>> columnsAndTypes = new HashMap<String, Class<?>>() {{
            put("MQCA_SERVICE_NAME",            String.class);
            put("MQCA_SERVICE_DESC",            String.class);
            put("MQIA_SERVICE_TYPE",            Integer.class);
            put("MQCA_SERVICE_START_ARGS",      String.class);
            put("MQCA_SERVICE_STOP_ARGS",       String.class);
            put("MQIA_SERVICE_CONTROL",         Integer.class);
            put("MQCA_ALTERATION_TIME",         String.class);
            put("MQCA_ALTERATION_DATE",         String.class);
            put("MQCA_STDERR_DESTINATION",      String.class);
            put("MQCA_STDOUT_DESTINATION",      String.class);
            put("MQCA_SERVICE_STOP_COMMAND",    String.class);
            put("MQCA_SERVICE_START_COMMAND",   String.class);
        }};

        final SimpleTable<String> resTable = new SimpleTable<>(columnsAndTypes);
        fillTableWithMessages(resTable, serviceRows);

        return resTable;
    }


    /**
     * Creates management entity for table of queue status parameters
     * That's the skeleton for static table definition
     * This function should not be used directly, access is provided via {@link com.snamp.connectors.ManagementConnectorBean.AttributeInfo}
     *
     * @return management entity of tabular type with columns represnting queue status variables
     */
    public final ManagementEntityType createQueueStatusTableType() {
        return createEntityTabularType(new HashMap<String, ManagementEntityType>() {{
            put("MQCA_Q_NAME",                  createStringType());
            put("MQIACF_Q_STATUS_TYPE",         createIntegerType());
            put("MQIA_CURRENT_Q_DEPTH",         createIntegerType());
            put("MQIA_OPEN_INPUT_COUNT",        createIntegerType());
            put("MQCACF_LAST_GET_DATE",         createStringType());
            put("MQCACF_LAST_GET_TIME",         createStringType());
            put("MQCACF_LAST_PUT_DATE",         createStringType());
            put("MQCACF_LAST_PUT_TIME",         createStringType());
            put("MQCACF_MEDIA_LOG_EXTENT_NAME", createStringType());
            put("MQIA_MONITORING_Q",            createIntegerType());
            put("MQIACF_OLDEST_MSG_AGE",        createIntegerType());
            put("MQIA_OPEN_OUTPUT_COUNT",       createIntegerType());
            put("MQIACF_Q_TIME_INDICATOR",      createStringType());
            put("MQIACF_UNCOMMITTED_MSGS",      createIntegerType());
        }});
    }


    /**
     * Creates management entity for table of channel status parameters
     * That's the skeleton for static table definition
     * This function should not be used directly, access is provided via {@link com.snamp.connectors.ManagementConnectorBean.AttributeInfo}
     *
     * @return management entity of tabular type with columns represnting channel status variables
     */
    public final ManagementEntityType createChannelStatusTableType() {
        return createEntityTabularType(new HashMap<String, ManagementEntityType>() {{
            put("MQCACH_CHANNEL_NAME",                          createStringType());
            put("MQIACH_CHANNEL_TYPE",                          createIntegerType());
            put("MQIACH_BUFFERS_RCVD/MQIACH_BUFFERS_RECEIVED",  createIntegerType());
            put("MQIACH_BUFFERS_SENT",                          createIntegerType());
            put("MQIACH_BYTES_RCVD/MQIACH_BYTES_RECEIVED",      createIntegerType());
            put("MQIACH_BYTES_SENT",                            createIntegerType());
            put("MQCACH_CHANNEL_START_DATE",                    createStringType());
            put("MQCACH_CHANNEL_START_TIME",                    createStringType());
            put("MQIACH_HDR_COMPRESSION",                       createStringType());
            put("MQIACH_MSG_COMPRESSION",                       createStringType());
            put("MQIACH_COMPRESSION_RATE",                      createStringType());
            put("MQIACH_COMPRESSION_TIME",                      createStringType());
            put("MQCACH_CONNECTION_NAME",                       createStringType());
            put("MQIACH_CHANNEL_INSTANCE_TYPE",                 createIntegerType());
            put("MQIACH_EXIT_TIME_INDICATOR",                   createStringType());
            put("MQIACH_HB_INTERVAL",                           createIntegerType());

            put("MQCACH_MCA_JOB_NAME",                          createStringType());
            put("MQCACH_LOCAL_ADDRESS",                         createStringType());
            put("MQCACH_LAST_MSG_DATE",                         createStringType());
            put("MQCACH_LAST_MSG_TIME",                         createStringType());
            put("MQIACH_MCA_STATUS",                            createIntegerType());
            put("MQCACH_MCA_USER_ID",                           createStringType());
            put("MQIA_MONITORING_CHANNEL",                      createIntegerType());
            put("MQIACH_MSGS",                                  createIntegerType());
            put("MQCACH_REMOTE_APPL_TAG",                       createStringType());
            put("MQCACH_SSL_CERT_ISSUER_NAME",                  createStringType());
            put("MQCACH_SSL_KEY_RESET_DATE",                    createStringType());
            put("MQCACH_SSL_KEY_RESET_TIME",                    createStringType());
            put("MQCACH_SSL_SHORT_PEER_NAME",                   createStringType());
            put("MQIACH_SSL_KEY_RESETS",                        createIntegerType());
            put("MQIACH_CHANNEL_STATUS",                        createIntegerType());
            put("MQIACH_STOP_REQUESTED",                        createIntegerType());

            put("MQIACH_CHANNEL_SUBSTATE",                      createIntegerType());
            put("MQIACH_CURRENT_SHARING_CONVS",                 createIntegerType());
            put("MQIACH_MAX_SHARING_CONVS",                     createIntegerType());
            put("MQCACH_REMOTE_VERSION",                        createStringType());
            put("MQCACH_REMOTE_PRODUCT",                        createStringType());
        }});
    }

    /**
     * Creates management entity for table of qmgr status parameters
     * That's the skeleton for static table definition
     * This function should not be used directly, access is provided via {@link com.snamp.connectors.ManagementConnectorBean.AttributeInfo}
     *
     * @return management entity of tabular type with columns represnting QMGR status variables
     */
    public final ManagementEntityType createQmgrStatusTableType() {
        return createEntityTabularType(new HashMap<String, ManagementEntityType>() {{
            put("MQCA_INSTALLATION_DESC",           createStringType());
            put("MQCACF_LOG_PATH",                  createStringType());
            put("MQCACF_Q_MGR_START_DATE",          createStringType());
            put("MQCACF_Q_MGR_START_TIME",          createStringType());
            put("MQCA_INSTALLATION_PATH",           createStringType());
            put("MQIACF_Q_MGR_STATUS",              createIntegerType());
            put("MQCA_Q_MGR_NAME",                  createStringType());
            put("MQCACF_RESTART_LOG_EXTENT_NAME",   createStringType());
            put("MQCA_INSTALLATION_NAME",           createStringType());
            put("MQCACF_CURRENT_LOG_EXTENT_NAME",   createStringType());
            put("MQIACF_CONNECTION_COUNT",          createIntegerType());
            put("MQIACF_CHINIT_STATUS",             createIntegerType());
            put("MQIACF_PERMIT_STANDBY",            createIntegerType());
            put("MQCACF_MEDIA_LOG_EXTENT_NAME",     createStringType());
            put("MQIACF_CMD_SERVER_STATUS",         createIntegerType());
        }});
    }

    /**
     * Creates management entity for table of service definition parameters
     * That's the skeleton for static table definition
     * This function should not be used directly, access is provided via {@link com.snamp.connectors.ManagementConnectorBean.AttributeInfo}
     *
     * @return management entity of tabular type with columns represnting service status variables
     */
    public final ManagementEntityType createServiceStatusTableType() {
        return createEntityTabularType(new HashMap<String, ManagementEntityType>() {{
            put("MQCA_SERVICE_NAME",            createStringType());
            put("MQCA_SERVICE_DESC",            createStringType());
            put("MQIA_SERVICE_TYPE",            createIntegerType());
            put("MQCA_SERVICE_START_ARGS",      createStringType());
            put("MQCA_SERVICE_STOP_ARGS",       createStringType());
            put("MQIA_SERVICE_CONTROL",         createIntegerType());
            put("MQCA_ALTERATION_TIME",         createStringType());
            put("MQCA_ALTERATION_DATE",         createStringType());
            put("MQCA_STDERR_DESTINATION",      createStringType());
            put("MQCA_STDOUT_DESTINATION",      createStringType());
            put("MQCA_SERVICE_STOP_COMMAND",    createStringType());
            put("MQCA_SERVICE_START_COMMAND",   createStringType());
        }});
    }

    /**
     * Helper function for simplified table row inserting for different types of entities
     *
     * @param table Table with string columns, the target of insertion
     * @param rows List of PCFMessages that need to be inserted
     */
    private static <T extends List<PCFMessage>> void fillTableWithMessages(SimpleTable<String> table, T rows) {
        for(final PCFMessage message : rows) // each message contains data about one entity - QMGR, Queue, Service or Channel
            table.addRow(new HashMap<String, Object>() {{
                final Enumeration paramEnum = message.getParameters(); // status of entity is handful of parameters
                while (paramEnum.hasMoreElements()) { // each column
                    final PCFParameter parameter = (PCFParameter) paramEnum.nextElement();
                    final Class parameterClazz = parameter.getValue().getClass();
                    if(!parameterClazz.isArray())
                        put(parameter.getParameterName(), parameter.getValue()); // Just normal value
                    else
                        put(parameter.getParameterName(), parameter.getStringValue()); // string representation like "[0, 0]"
                }
            }});
    }
}
