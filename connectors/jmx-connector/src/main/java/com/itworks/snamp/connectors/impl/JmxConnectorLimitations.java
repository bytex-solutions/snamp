package com.itworks.snamp.connectors.impl;

import com.itworks.snamp.licensing.AbstractLicenseLimitations;
import com.itworks.snamp.licensing.FrameworkServiceLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingException;
import org.apache.commons.collections4.Factory;
import org.osgi.framework.Version;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static com.itworks.snamp.core.AbstractServiceLibrary.RequiredServiceAccessor;
import static com.itworks.snamp.core.AbstractServiceLibrary.SimpleDependency;

/**
 * Represents license descriptor of the JMX connector.
 * @author Roman Sakno
 */
@XmlRootElement(name = "jmxConnectorLimitations")
public final class JmxConnectorLimitations extends AbstractLicenseLimitations implements FrameworkServiceLimitations<JmxConnector> {
    /**
     * Represents statically defined dependency of {@link com.itworks.snamp.licensing.LicenseReader} service.
     */
    public static final RequiredServiceAccessor<LicenseReader> licenseReader = new SimpleDependency<>(LicenseReader.class);

    /**
     * Initializes a new limitation descriptor for the JMX connector.
     */
    public JmxConnectorLimitations(){
        this(0L, 0L, "0.0");
    }

    private JmxConnectorLimitations(final long maxAttributeCount, final long maxInstanceCount, final String pluginVersion){
        this.maxAttributeCount = MaxRegisteredAttributeCountAdapter.createLimitation(maxAttributeCount);
        this.maxInstanceCount = MaxInstanceCountAdapter.createLimitation(maxInstanceCount);
        this.maxVersion = ConnectorVersionAdapter.createLimitation(pluginVersion);
    }

    private static final Factory<JmxConnectorLimitations> fallbackFactory = new Factory<JmxConnectorLimitations>() {
        @Override
        public JmxConnectorLimitations create() {
            return new JmxConnectorLimitations();
        }
    };

    /**
     * Gets currently loaded description of JMX connector license limitations.
     * @return The currently loaded license limitations.
     */
    public static JmxConnectorLimitations current(){
        return current(JmxConnectorLimitations.class, licenseReader, fallbackFactory);
    }

    private static final class MaxRegisteredAttributeCountAdapter extends RequirementParser<Comparable<Long>, Long, MaxValueLimitation<Long>> {

        public static final MaxValueLimitation<Long> createLimitation(final Long expectedValue){
            return new MaxValueLimitation<Long>(expectedValue) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("The maximum number of registered managementAttributes(%s) is reached.", requiredValue));
                }
            };
        }

        @Override
        public final MaxValueLimitation<Long> unmarshal(final Long expectedValue) {
            return createLimitation(expectedValue);
        }
    }

    private static final class MaxInstanceCountAdapter extends RequirementParser<Comparable<Long>, Long, MaxValueLimitation<Long>> {
        public static final MaxValueLimitation<Long> createLimitation(final Long expectedValue){
            return new MaxValueLimitation<Long>(expectedValue) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("The maximum number of JMX connector instances(%s) is reached.", requiredValue));
                }
            };
        }

        @Override
        public MaxValueLimitation<Long> unmarshal(final Long expectedValue) throws Exception {
            return createLimitation(expectedValue);
        }
    }

    private static final class ConnectorVersionAdapter extends RequirementParser<Version, String, VersionLimitation> {
        public static VersionLimitation createLimitation(final String expectedVersion){
            return new VersionLimitation(expectedVersion) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("'%s' version of JMX connector expected.", requiredValue));
                }
            };
        }

        @Override
        public VersionLimitation unmarshal(final String expectedVersion) throws Exception {
            return createLimitation(expectedVersion);
        }
    }


    @XmlJavaTypeAdapter(MaxRegisteredAttributeCountAdapter.class)
    @XmlElement(type = Long.class)
    private MaxValueLimitation<Long> maxAttributeCount;

    @XmlJavaTypeAdapter(MaxInstanceCountAdapter.class)
    @XmlElement(type = Long.class)
    private MaxValueLimitation<Long> maxInstanceCount;

    @XmlJavaTypeAdapter(ConnectorVersionAdapter.class)
    @XmlElement(type = String.class)
    private VersionLimitation maxVersion;

    public final void verifyMaxAttributeCount(final long currentAttributeCount) throws LicensingException{
        verify(maxAttributeCount, currentAttributeCount);
    }

    public final void verifyMaxInstanceCount(final long currentInstanceCount) throws LicensingException{
        verify(maxInstanceCount, currentInstanceCount);
    }

    @Override
    public void verifyPluginVersion(final Class<? extends JmxConnector> pluginImpl) throws LicensingException {
        verifyPluginVersion(maxVersion, pluginImpl);
    }
}
