package com.bytex.snamp.testing;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum SnampFeature {
    PLATFORM("platform-feature", "2.0.0"),
    JMX_CONNECTOR("jmx-connector-feature", "2.0.0"),
    COMPOSITE_CONNECTOR("composite-connector-feature", "2.0.0"),
    SNMP_GATEWAY("snmp-gateway-feature", "2.0.0"),
    JMX_GATEWAY("jmx-gateway-feature", "2.0.0"),
    SNMP_CONNECTOR("snmp-connector-feature", "2.0.0"),
    RSHELL_CONNECTOR("rshell-connector-feature", "2.0.0"),
    SSH_GATEWAY("ssh-gateway-feature", "2.0.0"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "2.0.0"),
    HTTP_GATEWAY("http-gateway-feature", "2.0.0"),
    NSCA_GATEWAY("nsca-gateway-feature", "2.0.0"),
    NRDP_GATEWAY("nrdp-gateway-feature", "2.0.0"),
    NAGIOS_GATEWAY("nagios-gateway-feature", "2.0.0"),
    SYSLOG_GATEWAY("syslog-gateway-feature", "2.0.0"),
    XMPP_GATEWAY("xmpp-gateway-feature", "2.0.0"),
    GROOVY_CONNECTOR("groovy-connector-feature", "2.0.0"),
    GROOVY_GATEWAY("groovy-gateway-feature", "2.0.0"),
    MODBUS_CONNECTOR("modbus-connector-feature", "2.0.0"),
    MDA_CONNECTOR("mda-connector-feature", "2.0.0"),
    MQ_CONNECTOR("mq-connector-feature", "2.0.0");

    final String featureName;
    final String version;

    SnampFeature(final String featureName, final String version){
        this.featureName = featureName;
        this.version = version;
    }

    public String getFeatureAbsoluteFileName(final String featureFileName){
        return TestUtils.join(new String[]{featureName, version, featureFileName}, '-');
    }

    public String getAbsoluteRepositoryPath(final String repositoryLocation,
                                            final String featureFileName){
        return TestUtils.join(new String[]{repositoryLocation, featureName, version, getFeatureAbsoluteFileName(featureFileName)}, '/');
    }
}
