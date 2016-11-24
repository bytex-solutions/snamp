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
    HTTP_ACCEPTOR("connectors-pack", "2.0.0", "snamp-http-acceptor"),
    JMX_CONNECTOR("connectors-pack", "2.0.0", "snamp-jmx-connector"),
    ZIPKIN_CONNECTOR("connectors-pack", "2.0.0", "snamp-zipkin-connector"),
    COMPOSITE_CONNECTOR("connectors-pack", "2.0.0", "snamp-composite-connector"),
    SNMP_GATEWAY("snmp-gateway-feature", "2.0.0", "snmp-gateway-feature"),
    INFLUX_GATEWAY("influx-gateway-feature", "2.0.0", "influx-gateway-feature"),
    JMX_GATEWAY("jmx-gateway-feature", "2.0.0", "jmx-gateway-feature"),
    SNMP_CONNECTOR("connectors-pack", "2.0.0", "snamp-snmp-connector"),
    RSHELL_CONNECTOR("connectors-pack", "2.0.0", "snamp-rshell-connector"),
    SSH_GATEWAY("ssh-gateway-feature", "2.0.0", "ssh-gateway-feature"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "2.0.0", "wrapped-libs-for-tests"),
    HTTP_GATEWAY("http-gateway-feature", "2.0.0", "http-gateway-feature"),
    NSCA_GATEWAY("nsca-gateway-feature", "2.0.0", "nsca-gateway-feature"),
    NRDP_GATEWAY("nrdp-gateway-feature", "2.0.0", "nrdp-gateway-feature"),
    NAGIOS_GATEWAY("nagios-gateway-feature", "2.0.0", "nagios-gateway-feature"),
    SYSLOG_GATEWAY("syslog-gateway-feature", "2.0.0", "syslog-gateway-feature"),
    XMPP_GATEWAY("xmpp-gateway-feature", "2.0.0", "xmpp-gateway-feature"),
    GROOVY_CONNECTOR("connectors-pack", "2.0.0", "snamp-groovy-connector"),
    GROOVY_GATEWAY("groovy-gateway-feature", "2.0.0", "groovy-gateway-feature"),
    MODBUS_CONNECTOR("connectors-pack", "2.0.0", "snamp-modbus-connector"),
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
