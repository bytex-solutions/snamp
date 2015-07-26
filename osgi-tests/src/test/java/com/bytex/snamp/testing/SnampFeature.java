package com.bytex.snamp.testing;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum SnampFeature {
    PLATFORM("platform-feature", "1.0.0"),
    JMX_CONNECTOR("jmx-connector-feature", "1.0.0"),
    WMQ_CONNECTOR("ibmwmq-connector-feature", "1.0.0"),
    SNMP_ADAPTER("snmp-adapter-feature", "1.0.0"),
    JMX_ADAPTER("jmx-adapter-feature", "1.0.0"),
    SNMP_CONNECTOR("snmp-connector-feature", "1.0.0"),
    RSHELL_CONNECTOR("rshell-connector-feature", "1.0.0"),
    SSH_ADAPTER("ssh-adapter-feature", "1.0.0"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "1.0.0"),
    HTTP_ADAPTER("http-adapter-feature", "1.0.0"),
    RESOURCE_AGGREGATOR("aggregator-connector-feature", "1.0.0"),
    NSCA_ADAPTER("nsca-adapter-feature", "1.0.0"),
    NRDP_ADAPTER("nrdp-adapter-feature", "1.0.0"),
    NAGIOS_ADAPTER("nagios-adapter-feature", "1.0.0"),
    SYSLOG_ADAPTER("syslog-adapter-feature", "1.0.0"),
    XMPP_ADAPTER("xmpp-adapter-feature", "1.0.0"),
    GROOVY_CONNECTOR("groovy-connector-feature", "1.0.0"),
    GROOVY_ADAPTER("groovy-adapter-feature", "1.0.0"),
    MODBUS_CONNECTOR("modbus-connector-feature", "1.0.0");

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
