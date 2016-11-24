package com.bytex.snamp.testing;

import java.util.Objects;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum SnampFeature {
    PLATFORM("platform", "2.0.0", "snamp-core", "snamp-management"),
    HTTP_ACCEPTOR("http-acceptor-feature", "2.0.0", "http-acceptor-feature"),
    JMX_CONNECTOR("jmx-connector-feature", "2.0.0", "jmx-connector-feature"),
    ZIPKIN_CONNECTOR("zipkin-connector-feature", "2.0.0", "zipkin-connector-feature"),
    COMPOSITE_CONNECTOR("composite-connector-feature", "2.0.0", "composite-connector-feature"),
    SNMP_GATEWAY("snmp-gateway-feature", "2.0.0", "snmp-gateway-feature"),
    INFLUX_GATEWAY("influx-gateway-feature", "2.0.0", "influx-gateway-feature"),
    JMX_GATEWAY("jmx-gateway-feature", "2.0.0", "jmx-gateway-feature"),
    SNMP_CONNECTOR("snmp-connector-feature", "2.0.0", "snmp-connector-feature"),
    RSHELL_CONNECTOR("rshell-connector-feature", "2.0.0", "rshell-connector-feature"),
    SSH_GATEWAY("ssh-gateway-feature", "2.0.0", "ssh-gateway-feature"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "2.0.0", "wrapped-libs-for-tests"),
    HTTP_GATEWAY("http-gateway-feature", "2.0.0", "http-gateway-feature"),
    NSCA_GATEWAY("nsca-gateway-feature", "2.0.0", "nsca-gateway-feature"),
    NRDP_GATEWAY("nrdp-gateway-feature", "2.0.0", "nrdp-gateway-feature"),
    NAGIOS_GATEWAY("nagios-gateway-feature", "2.0.0", "nagios-gateway-feature"),
    SYSLOG_GATEWAY("syslog-gateway-feature", "2.0.0", "syslog-gateway-feature"),
    XMPP_GATEWAY("xmpp-gateway-feature", "2.0.0", "xmpp-gateway-feature"),
    GROOVY_CONNECTOR("groovy-connector-feature", "2.0.0", "groovy-connector-feature"),
    GROOVY_GATEWAY("groovy-gateway-feature", "2.0.0", "groovy-gateway-feature"),
    MODBUS_CONNECTOR("modbus-connector-feature", "2.0.0", "modbus-connector-feature"),
    WEBCONSOLE("webconsole-feature", "2.0.0", "webconsole-feature");

    final String[] featureNames;
    private final String artifactId;
    final String version;

    SnampFeature(final String artifactId, final String version, final String... featureNames){
        this.artifactId = artifactId;
        this.version = version;
        this.featureNames = Objects.requireNonNull(featureNames);
    }

    private String getArtifactAbsoluteFileName(final String featureFileName){
        return TestUtils.join(new String[]{artifactId, version, featureFileName}, '-');
    }

    public String getAbsoluteRepositoryPath(final String repositoryLocation,
                                            final String featureFileName){
        return TestUtils.join(new String[]{repositoryLocation, artifactId, version, getArtifactAbsoluteFileName(featureFileName)}, '/');
    }
}
