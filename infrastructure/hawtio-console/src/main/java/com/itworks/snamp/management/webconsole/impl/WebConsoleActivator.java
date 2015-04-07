package com.itworks.snamp.management.webconsole.impl;

import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.licensing.LicenseLoader;
import com.itworks.snamp.licensing.LicenseManager;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import javax.management.*;
import javax.management.openmbean.OpenDataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Map;

/**
 * Represents activator of the SNAMP Web Console.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleActivator extends AbstractServiceLibrary {
    /**
     * Represents name of the system property that references the external file with SNAMP license.
     */
    private static final String BOOT_LICENSE_SYSTEM_PROP = "com.itworks.snamp.licensing.file";

    private static final class LicenseTracker extends ProvidedService<ManagedService, LicenseLoader>{

        private LicenseTracker() {
            super(ManagedService.class);
        }

        @Override
        protected LicenseLoader activateService(final Map<String, Object> identity,
                                                final RequiredService<?>... dependencies) {
            identity.put(Constants.SERVICE_PID, LicenseLoader.LICENSE_PID);
            return getActivationPropertyValue(LICENSE_MANAGER_ACTIVATION_PROPERTY).getLicenseLoader();
        }
    }

    private static final ActivationProperty<LicenseManager> LICENSE_MANAGER_ACTIVATION_PROPERTY = defineActivationProperty(LicenseManager.class);
    private final LicenseManager licensingManager;
    private final ObjectName licensingManagerName;

    private WebConsoleActivator() throws OpenDataException, MalformedObjectNameException {
        super(new LicenseTracker());
        licensingManagerName = new ObjectName(LicenseManager.OBJECT_NAME);
        licensingManager = LicenseManager.getInstance();
    }

    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, IOException {
        final File licenseFile = new File(System.getProperty(BOOT_LICENSE_SYSTEM_PROP, "snamp.lic"));
        if(licenseFile.exists())
            try(final InputStream licenseStream = new FileInputStream(licenseFile)){
                licensingManager.getLicenseLoader().loadLicense(licenseStream);
            }
        ManagementFactory.getPlatformMBeanServer().registerMBean(licensingManager, licensingManagerName);
    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties,
                            final RequiredService<?>... dependencies) {
        activationProperties.publish(LICENSE_MANAGER_ACTIVATION_PROPERTY, licensingManager);
    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
    }

    @Override
    protected void shutdown() throws MBeanRegistrationException, InstanceNotFoundException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(licensingManagerName);
    }
}
