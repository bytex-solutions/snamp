package com.itworks.snamp.licensing;

import com.itworks.snamp.jmx.OpenMBean;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import javax.management.openmbean.OpenDataException;
import java.util.Dictionary;

/**
 * Exposes licensing information through JMX.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class LicenseManager extends OpenMBean {
    /**
     * The name of the SNAMP licensing management bean.
     */
    public static final String OBJECT_NAME = "com.itworks.snamp.management:type=SnampLicenseManager";
    private static volatile LicenseManager INSTANCE;

    private LicenseManager() throws OpenDataException {
        super(new InstalledResourceAdaptersAttribute(),
                new NumberOfManagedResourcesAttribute(),
                new LicenseAttribute());
    }

    private static synchronized LicenseManager getInstanceSynchronized() throws OpenDataException {
        if(INSTANCE == null)
            INSTANCE = new LicenseManager();
        return INSTANCE;
    }

    public static LicenseManager getInstance() throws OpenDataException {
        return INSTANCE == null ? getInstanceSynchronized() : INSTANCE;
    }

}
