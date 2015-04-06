package com.itworks.snamp.management.webconsole.impl;

import com.itworks.snamp.core.AbstractBundleActivator;
import com.itworks.snamp.licensing.LicenseManager;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.OpenDataException;
import java.lang.management.ManagementFactory;
import java.util.Collection;

/**
 * Represents activator of the SNAMP Web Console
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class WebConsoleActivator extends AbstractBundleActivator {
    /**
     * Represents name of the system property that references the external file with SNAMP license.
     */
    private static final String BOOT_LICENSE_SYSTEM_PROP = "com.itworks.snamp.licensing.file";

    private final LicenseManager licensingManager;
    private final ObjectName licensingManagerName;

    private WebConsoleActivator() throws OpenDataException, MalformedObjectNameException {
        licensingManagerName = new ObjectName(LicenseManager.OBJECT_NAME);
        licensingManager = LicenseManager.getInstance();
    }

    @Override
    protected void start(final BundleContext context, final Collection<RequiredService<?>> bundleLevelDependencies) throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        ManagementFactory.getPlatformMBeanServer().registerMBean(licensingManager, licensingManagerName);
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {

    }


    @Override
    protected void deactivate(final BundleContext context, final ActivationPropertyReader activationProperties) throws Exception {

    }

    @Override
    protected void shutdown(final BundleContext context) throws MBeanRegistrationException, InstanceNotFoundException {
        ManagementFactory.getPlatformMBeanServer().unregisterMBean(licensingManagerName);
    }
}
