package com.itworks.snamp.licensing.impl;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.internal.annotations.MethodStub;
import com.itworks.snamp.licensing.LicenseReader;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents implementation of the SNAMP licensing bundle. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class LicenseBundleActivator extends AbstractServiceLibrary {
    private static final ActivationProperty<XmlLicenseReader> LICENSE_READER_ACTIVATION_PROPERTY = defineActivationProperty(XmlLicenseReader.class);

    private static final class XmlLicenseReaderProvidedService extends ProvidedService<LicenseReader, XmlLicenseReader>{

        private XmlLicenseReaderProvidedService() {
            super(LicenseReader.class);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected XmlLicenseReader activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            return getActivationPropertyValue(LICENSE_READER_ACTIVATION_PROPERTY);
        }
    }

    private static final class LicenseTrackerProvidedService extends ProvidedService<ManagedService, XmlLicenseReader>{
        private LicenseTrackerProvidedService(){
            super(ManagedService.class);
        }

        @Override
        protected XmlLicenseReader activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws Exception {
            identity.put(Constants.SERVICE_PID, XmlLicenseReader.LICENSE_PID);
            return getActivationPropertyValue(LICENSE_READER_ACTIVATION_PROPERTY);
        }
    }

    public LicenseBundleActivator(){
        super(new XmlLicenseReaderProvidedService(), new LicenseTrackerProvidedService());
    }


    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     */
    @Override
    @MethodStub
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {

    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     */
    @Override
    @MethodStub
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {
        final XmlLicenseReader reader = new XmlLicenseReader();
        activationProperties.publish(LICENSE_READER_ACTIVATION_PROPERTY, reader);
        //load configuration from file if it is necessary
        bootLicense();
    }

    private void bootLicense() throws IOException {
        final BundleContext context = Utils.getBundleContextByObject(this);
        final File licenseFile = new File(System.getProperty(XmlLicenseReader.BOOT_LICENSE_SYSTEM_PROP, "snamp.lic"));
        if (licenseFile.exists()) {
            final ServiceReferenceHolder<ConfigurationAdmin> configService = new ServiceReferenceHolder<>(context,
                    ConfigurationAdmin.class);
            try {
                final byte[] licenseContent = Files.readAllBytes(Paths.get(licenseFile.toURI()));
                final Configuration config = configService.getService()
                        .getConfiguration(XmlLicenseReader.LICENSE_PID, null);
                final Hashtable<String, Object> entry = new Hashtable<>();
                entry.put(XmlLicenseReader.LICENSE_CONTENT_ENTRY, licenseContent);
                config.update(entry);
            }
            finally {
                configService.release(context);
            }
        }
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected void deactivate(final ActivationPropertyReader activationProperties) {
    }
}
