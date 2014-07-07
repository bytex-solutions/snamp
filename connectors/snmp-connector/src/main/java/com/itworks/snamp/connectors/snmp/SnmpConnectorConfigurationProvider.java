package com.itworks.snamp.connectors.snmp;

/**
 * Represents SNMP connector configuration descriptor.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpConnectorConfigurationProvider {
    //adapter related parameters
    public static final String COMMUNITY_PARAM = "community";
    public static final String ENGINE_ID_PARAM = "engineID";
    public static final String USER_NAME_PARAM = "userName";
    public static final String LOCAL_ADDRESS_PARAM = "localAddress";
    //attribute related parameters
    public static final String SNMP_CONVERSION_FORMAT = "snmpConversionFormat";
    //event related parameters
    public static final String SEVERITY_PARAM = "severity";
    public static final String MESSAGE_TEMPLATE = "messageTemplate";
}
