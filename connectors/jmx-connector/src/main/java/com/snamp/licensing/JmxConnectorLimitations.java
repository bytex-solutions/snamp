package com.snamp.licensing;

import com.snamp.Activator;
import com.snamp.connectors.AbstractManagementConnectorFactory;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Represents license descriptor of the JMX connector.
 * @author roman
 */
@XmlRootElement(name = "jmxConnectorLimitations")
public final class JmxConnectorLimitations extends AbstractLicenseLimitations implements PluginLicenseLimitations<AbstractManagementConnectorFactory> {

    /**
     * Initializes a new limitation descriptor for the JMX connector.
     */
    public JmxConnectorLimitations(){

    }

    private JmxConnectorLimitations(final long maxAttributeCount, final long maxInstanceCount, final String pluginVersion){
        this.maxAttributeCount = MaxRegisteredAttributeCountAdapter.createLimitation(maxAttributeCount);
        this.maxInstanceCount = MaxInstanceCountAdapter.createLimitation(maxInstanceCount);
        this.maxVersion = ConnectorVersionAdapter.createLimitation(pluginVersion);

    }

    private static final Activator<JmxConnectorLimitations> fallbackFactory = new Activator<JmxConnectorLimitations>() {
        @Override
        public JmxConnectorLimitations newInstance() {
            return new JmxConnectorLimitations(0L, 0L, "0.0");
        }
    };


    /**
     * Returns the currently loaded limitations.
     * @return
     */
    public final static JmxConnectorLimitations current(){
        return current(JmxConnectorLimitations.class, fallbackFactory);
    }

    private static final class MaxRegisteredAttributeCountAdapter extends RequirementParser<Comparable<Long>, Long, MaxValueLimitation<Long>> {

        public static final MaxValueLimitation<Long> createLimitation(final Long expectedValue){
            return new MaxValueLimitation<Long>(expectedValue) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("The maximum number of registered attributes(%s) is reached.", requiredValue));
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

    private static final class ConnectorVersionAdapter extends RequirementParser<String, String, VersionLimitation> {
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

    /**
     * @param pluginImpl
     * @throws LicensingException
     *
     */
    @Override
    public final void verifyPluginVersion(final Class<? extends AbstractManagementConnectorFactory> pluginImpl) throws LicensingException {
        verify(maxVersion, pluginImpl.getPackage().getImplementationVersion());
    }
}
