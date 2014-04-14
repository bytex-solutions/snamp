package com.itworks.snamp;

import org.ops4j.pax.exam.options.*;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

/**
 * Represents SNAMP artifacts.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public enum SnampArtifact {
    CORLIB("corlib", "1.0.0"),
    CONFIG_BUNDLE("config-bundle", "1.0.0"),
    LICENSING_BUNDLE("licensing-bundle", "1.0.0");
    public static final String GROUP_ID = "com.itworks.snamp";

    private final String artifactId;
    private final String version;

    private SnampArtifact(final String artifactId, final String version){
        this.artifactId = artifactId;
        this.version = version;
    }

    public static final SnampArtifact[] BASIC_SET = new SnampArtifact[]{
        CORLIB,
        CONFIG_BUNDLE,
        LICENSING_BUNDLE
    };

    public MavenArtifactProvisionOption getReference(){
        return mavenBundle(GROUP_ID, artifactId, version);
    }

    public static AbstractProvisionOption<?>[] makeReferences(final SnampArtifact... artifacts){
        final AbstractProvisionOption<?>[] result = new AbstractProvisionOption<?>[artifacts.length];
        for(int i = 0; i < artifacts.length; i++)
            result[i] = artifacts[i].getReference();
        return result;
    }

    public static AbstractProvisionOption<?>[] makeBasicSet(){
        return makeReferences(BASIC_SET);
    }
}
