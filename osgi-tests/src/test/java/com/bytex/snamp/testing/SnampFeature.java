package com.bytex.snamp.testing;

import java.util.Objects;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public enum SnampFeature {
    PLATFORM("core", "2.0.0", "snamp-core", "snamp-web-support"),
    OS_SUPERVISOR("connectors", "2.0.0", "snamp-openstack-supervisor"),
    STUB_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-stub-connector"),
    HTTP_ACCEPTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-stream-connector", "snamp-http-acceptor"),
    JMX_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-jmx-connector"),
    ACTUATOR_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-actuator-connector"),
    ZIPKIN_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-stream-connector", "snamp-zipkin-connector"),
    COMPOSITE_CONNECTOR("connectors", "2.0.0","snamp-default-supervisor", "snamp-composite-connector"),
    SNMP_GATEWAY("gateways", "2.0.0", "snamp-snmp-gateway"),
    INFLUX_GATEWAY("gateways", "2.0.0", "snamp-influx-gateway"),
    JMX_GATEWAY("gateways", "2.0.0", "snamp-jmx-gateway"),
    SNMP_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-snmp-connector"),
    RSHELL_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-rshell-connector"),
    SSH_GATEWAY("gateways", "2.0.0", "snamp-ssh-gateway"),
    WRAPPED_LIBS("wrapped-libs-for-tests", "2.0.0", "wrapped-libs-for-tests"),
    HTTP_GATEWAY("gateways", "2.0.0", "snamp-http-gateway"),
    NSCA_GATEWAY("gateways", "2.0.0", "snamp-nsca-gateway"),
    NRDP_GATEWAY("gateways", "2.0.0", "snamp-nrdp-gateway"),
    NAGIOS_GATEWAY("gateways", "2.0.0", "snamp-nagios-gateway"),
    SYSLOG_GATEWAY("gateways", "2.0.0", "snamp-syslog-gateway"),
    XMPP_GATEWAY("gateways", "2.0.0", "snamp-xmpp-gateway"),
    SMTP_GATEWAY("gateways", "2.0.0", "snamp-smtp-gateway"),
    GROOVY_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-groovy-connector"),
    GROOVY_GATEWAY("gateways", "2.0.0", "snamp-groovy-gateway"),
    MODBUS_CONNECTOR("connectors", "2.0.0", "snamp-default-supervisor", "snamp-modbus-connector"),
    STANDARD_TOOLS("standard", "2.0.0", "snamp-management", "snamp-data-analysis", "snamp-web-console"),
    INTEGRATION_TOOLS("integration", "2.0.0", "snamp-discovery-over-http");

    private static final String FEATURES_FILE_NAME = "features.xml";
    final String[] featureNames;
    private final SnampGroupId groupId;
    private final String artifactId;
    final String version;

    SnampFeature(final String artifactId, final String version, final String... featureNames) {
        this.artifactId = artifactId;
        this.version = version;
        this.featureNames = Objects.requireNonNull(featureNames);
        groupId = SnampGroupId.FEATURES;
    }

    String getFeatureFile(){
        final String artifactFileName = TestUtils.join(new String[]{artifactId, version, FEATURES_FILE_NAME}, '-');
        return TestUtils.join(new String[]{groupId.getAbsolutePath(), artifactId, version, artifactFileName}, '/');
    }

    @Override
    public String toString() {
        return groupId.toString() + ':' + artifactId;
    }
}
