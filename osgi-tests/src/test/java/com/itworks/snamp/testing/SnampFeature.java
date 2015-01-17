package com.itworks.snamp.testing;

import org.ops4j.pax.exam.options.*;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum SnampFeature {
    CORLIB("framework", "1.0.0"),
    LICENSING_BUNDLE("licensing-bundle", "1.0.0"),
    JMX_CONNECTOR("jmx-connector", "1.0.0"),
    SNMP4J("snmp4j", "2.2.3"),
    SSHJ("sshj", "0.10.0"),
    SNMP_ADAPTER("snmp-adapter", "1.0.0"),
    MANAGEMENT("management-bundle", "1.0.0"),
    WEB_CONSOLE("web-console", "1.0.0"),
    REST_ADAPTER("rest-adapter", "1.0.0"),
    JMX_ADAPTER("jmx-adapter", "1.0.0"),
    SNMP_CONNECTOR("snmp-connector", "1.0.0"),
    RSHELL_CONNECTOR("rshell-connector", "1.0.0"),
    SSH_ADAPTER("ssh-adapter", "1.0.0"),
    JAAS_CONFIG("jaas-config", "1.0.0")
    ;

    public static final String GROUP_ID = "com.itworks.snamp";

    private final String artifactId;
    private final String version;

    private SnampFeature(final String artifactId, final String version){
        this.artifactId = artifactId;
        this.version = version;
    }

    public static final SnampFeature[] BASIC_SET = new SnampFeature[]{
        CORLIB,
        LICENSING_BUNDLE
    };

    public MavenArtifactProvisionOption getReference(){
        return mavenBundle(GROUP_ID, artifactId, version);
    }

    public static AbstractProvisionOption<?>[] makeReferences(final SnampFeature... artifacts){
        final AbstractProvisionOption<?>[] result = new AbstractProvisionOption<?>[artifacts.length];
        for(int i = 0; i < artifacts.length; i++)
            result[i] = artifacts[i].getReference();
        return result;
    }

    public static AbstractProvisionOption<?>[] makeBasicSet(){
        return makeReferences(BASIC_SET);
    }

    public String getFeatureAbsoluteFileName(final String featureFileName){
        return TestUtils.join(new String[]{featureName, version, featureFileName}, '-');
    }

    public String getAbsoluteRepositoryPath(final String repositoryLocation,
                                            final String featureFileName){
        return TestUtils.join(new String[]{repositoryLocation, featureName, version, getFeatureAbsoluteFileName(featureFileName)}, '/');
    }
}
