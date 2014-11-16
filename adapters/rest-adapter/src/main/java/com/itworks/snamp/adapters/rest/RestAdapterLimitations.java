package com.itworks.snamp.adapters.rest;

import com.google.common.base.Supplier;
import com.itworks.snamp.licensing.AbstractLicenseLimitations;
import com.itworks.snamp.licensing.FrameworkServiceLimitations;
import com.itworks.snamp.licensing.LicenseReader;
import com.itworks.snamp.licensing.LicensingException;
import org.osgi.framework.Version;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import static com.itworks.snamp.core.AbstractBundleActivator.RequiredServiceAccessor;
import static com.itworks.snamp.core.AbstractBundleActivator.SimpleDependency;

/**
 * Represents license limitations for REST adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "restAdapterLimitations")
public final class RestAdapterLimitations extends AbstractLicenseLimitations implements FrameworkServiceLimitations<RestAdapter> {
    public static final RequiredServiceAccessor<LicenseReader> licenseReader = new SimpleDependency<>(LicenseReader.class);

    static final Supplier<RestAdapterLimitations> fallbackFactory = new Supplier<RestAdapterLimitations>() {
        @Override
        public RestAdapterLimitations get() {
            return new RestAdapterLimitations();
        }
    };

    public RestAdapterLimitations(){
        this("0.0");
    }

    private RestAdapterLimitations(final String version){
        maxVersion = PluginVersionLimitationAdapter.createLimitation(version);
    }

    /**
     * Returns the currently loaded limitations.
     * @return The currently loaded limitations.
     */
    public static RestAdapterLimitations current(){
        return current(RestAdapterLimitations.class, licenseReader, fallbackFactory);
    }

    private static final class PluginVersionLimitationAdapter extends RequirementParser<Version, String, VersionLimitation> {

        public static VersionLimitation createLimitation(final String requiredVersion){
            return new VersionLimitation(requiredVersion) {
                @Override
                public LicensingException createException() {
                    return new LicensingException(String.format("'%s' version of REST adapter expected.", requiredValue));
                }
            };
        }

        @Override
        public VersionLimitation unmarshal(final String requiredVersion) {
            return createLimitation(requiredVersion);
        }
    }

    @XmlJavaTypeAdapter(PluginVersionLimitationAdapter.class)
    @XmlElement(type = String.class)
    private VersionLimitation maxVersion;

    /**
     * Verifies version of the SNAMP-specific version.
     *
     * @param serviceContract Type of the service contract to verify.
     * @throws com.itworks.snamp.licensing.LicensingException Actual version of the service doesn't met to license requirements.
     */
    @Override
    public void verifyServiceVersion(final Class<? extends RestAdapter> serviceContract) throws LicensingException {
        verifyServiceVersion(maxVersion, serviceContract);
    }
}