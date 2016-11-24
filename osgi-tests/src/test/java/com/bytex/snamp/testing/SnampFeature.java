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
    SNMP_GATEWAY("gateways-pack", "2.0.0", "snamp-snmp-gateway"),
    INFLUX_GATEWAY("gateways-pack", "2.0.0", "snamp-influx-gateway"),
    JMX_GATEWAY("gateways-pack", "2.0.0", "snamp-jmx-gateway"),
    SNMP_CONNECTOR("connectors-pack", "2.0.0", "snamp-snmp-connector"),
    RSHELL_CONNECTOR("connectors-pack", "2.0.0", "snamp-rshell-connector"),
    SSH_GATEWAY("gateways-pack", "2.0.0", "snamp-ssh-gateway"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "2.0.0", "wrapped-libs-for-tests"),
    HTTP_GATEWAY("gateways-pack", "2.0.0", "snamp-http-gateway"),
    NSCA_GATEWAY("gateways-pack", "2.0.0", "snamp-nsca-gateway"),
    NRDP_GATEWAY("gateways-pack", "2.0.0", "snamp-nrdp-gateway"),
    NAGIOS_GATEWAY("gateways-pack", "2.0.0", "snamp-nagios-gateway"),
    SYSLOG_GATEWAY("gateways-pack", "2.0.0", "snamp-syslog-gateway"),
    XMPP_GATEWAY("gateways-pack", "2.0.0", "snamp-xmpp-gateway"),
    GROOVY_CONNECTOR("connectors-pack", "2.0.0", "snamp-groovy-connector"),
    GROOVY_GATEWAY("gateways-pack", "2.0.0", "snamp-groovy-gateway"),
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
