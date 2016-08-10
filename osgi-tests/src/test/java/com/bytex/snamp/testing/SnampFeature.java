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
    SNMP_GATEWAY("snmp-adapter-feature", "2.0.0"),
    JMX_GATEWAY("jmx-adapter-feature", "2.0.0"),
    SNMP_CONNECTOR("snmp-connector-feature", "2.0.0"),
    RSHELL_CONNECTOR("rshell-connector-feature", "2.0.0"),
    SSH_GATEWAY("ssh-adapter-feature", "2.0.0"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "2.0.0"),
    HTTP_GATEWAY("http-adapter-feature", "2.0.0"),
    RESOURCE_AGGREGATOR("aggregator-connector-feature", "2.0.0"),
    NSCA_GATEWAY("nsca-adapter-feature", "2.0.0"),
    NRDP_GATEWAY("nrdp-adapter-feature", "2.0.0"),
    NAGIOS_GATEWAY("nagios-adapter-feature", "2.0.0"),
    SYSLOG_GATEWAY("syslog-adapter-feature", "2.0.0"),
    XMPP_GATEWAY("xmpp-adapter-feature", "2.0.0"),
    GROOVY_CONNECTOR("groovy-connector-feature", "2.0.0"),
    GROOVY_GATEWAY("groovy-adapter-feature", "2.0.0"),
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
