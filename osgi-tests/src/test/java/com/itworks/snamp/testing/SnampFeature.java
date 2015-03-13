package com.itworks.snamp.testing;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum SnampFeature {
    PLATFORM("platform-feature", "1.0.0"),
    LICENSING("licensing-feature", "1.0.0"),
    JMX_CONNECTOR("jmx-connector-feature", "1.0.0"),
    SNMP_ADAPTER("snmp-adapter-feature", "1.0.0"),
    REST_ADAPTER("http-adapter-feature", "1.0.0"),
    JMX_ADAPTER("jmx-adapter-feature", "1.0.0"),
    SNMP_CONNECTOR("snmp-connector-feature", "1.0.0"),
    RSHELL_CONNECTOR("rshell-connector-feature", "1.0.0"),
    SSH_ADAPTER("ssh-adapter-feature", "1.0.0"),
    APACHE_DS("apache-ds-feature", "1.0.0"),
    HTTP_ADAPTER("http-adapter-feature", "1.0.0");

    final String featureName;
    final String version;

    private SnampFeature(final String featureName, final String version){
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
