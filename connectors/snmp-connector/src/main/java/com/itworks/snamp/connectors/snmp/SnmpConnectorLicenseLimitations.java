package com.itworks.snamp.connectors.snmp;

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
@XmlRootElement(name = "snmpConnectorLimitations")
public final class SnmpConnectorLicenseLimitations extends AbstractLicenseLimitations implements FrameworkServiceLimitations<SnmpResourceConnector> {
    /**
     * Represents statically defined dependency of {@link com.itworks.snamp.licensing.LicenseReader} service.
     */
    public static final RequiredServiceAccessor<LicenseReader> licenseReader = new SimpleDependency<>(LicenseReader.class);

    /**
     * Initializes a new limitation descriptor for the JMX connector.
     */
    public SnmpConnectorLicenseLimitations(){
        this(0L, 0L, false, "0.0");
    }

    private SnmpConnectorLicenseLimitations(final long maxAttributeCount,
                                            final long maxInstanceCount,
                                            final boolean authenticationEnabled,
                                            final String pluginVersion){
        this.maxAttributeCount = MaxRegisteredAttributeCountAdapter.createLimitation(maxAttributeCount);
        this.maxInstanceCount = MaxInstanceCountAdapter.createLimitation(maxInstanceCount);
        this.maxVersion = ConnectorVersionAdapter.createLimitation(pluginVersion);
        this.authenticationEnabled = AuthenticationFeatureLimitationAdapter.createLimitation(authenticationEnabled);
    }

    static final Factory<SnmpConnectorLicenseLimitations> fallbackFactory = new Factory<SnmpConnectorLicenseLimitations>() {
        @Override
        public SnmpConnectorLicenseLimitations create() {
            return new SnmpConnectorLicenseLimitations();
        }
    };

    /**
     * Gets currently loaded description of JMX connector license limitations.
     * @return The currently loaded license limitations.
     */
    static SnmpConnectorLicenseLimitations current(){
        return current(SnmpConnectorLicenseLimitations.class, licenseReader, fallbackFactory);
    }

    private static final class MaxRegisteredAttributeCountAdapter extends RequirementParser<Comparable<Long>, Long, MaxValueLimitation<Long>> {

        public static MaxValueLimitation<Long> createLimitation(final Long expectedValue){
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
        public static MaxValueLimitation<Long> createLimitation(final Long expectedValue){
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

    private static final class AuthenticationFeatureLimitationAdapter extends RequirementParser<Boolean, Boolean, ExactLimitation<Boolean>>{

        public static ExactLimitation<Boolean> createLimitation(final boolean authenticationEnabled){
            return new ExactLimitation<Boolean>(authenticationEnabled) {
                @Override
                public LicensingException createException() {
                    return new LicensingException("SNMPv3 is not allowed.");
                }
            };
        }

        /**
         * Convert a value type to a bound type.
         *
         * @param v The value to be converted. Can be null.
         * @throws Exception if there's an error during the conversion. The caller is responsible for
         *                   reporting the error to the user through {@link javax.xml.bind.ValidationEventHandler}.
         */
        @Override
        public final ExactLimitation<Boolean> unmarshal(final Boolean v) throws Exception {
            return createLimitation(v != null && v);
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

    @XmlJavaTypeAdapter(AuthenticationFeatureLimitationAdapter.class)
    @XmlElement(type = Boolean.class)
    private ExactLimitation<Boolean> authenticationEnabled;

    final void verifyAuthenticationFeature() throws LicensingException{
        verify(authenticationEnabled, true);
    }

    final void verifyMaxAttributeCount(final long currentAttributeCount) throws LicensingException{
        verify(maxAttributeCount, currentAttributeCount);
    }

    final void verifyMaxInstanceCount(final long currentInstanceCount) throws LicensingException{
        verify(maxInstanceCount, currentInstanceCount);
    }

    @Override
    public void verifyServiceVersion(final Class<? extends SnmpResourceConnector> serviceContract) throws LicensingException {
        verifyServiceVersion(maxVersion, serviceContract);
    }

    public void verifyServiceVersion() throws LicensingException{
        verifyServiceVersion(SnmpResourceConnector.class);
    }
}
