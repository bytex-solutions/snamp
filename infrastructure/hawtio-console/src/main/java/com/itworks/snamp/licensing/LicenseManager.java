package com.itworks.snamp.licensing;

import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.OpenDataException;
import javax.xml.bind.JAXBException;

/**
 * Exposes licensing information through JMX.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class LicenseManager extends OpenMBean implements LicenseProvider {
    /**
     * The name of the SNAMP licensing management bean.
     */
    public static final String OBJECT_NAME = "com.itworks.snamp.management:type=SnampLicenseManager";
    private static volatile LicenseManager INSTANCE;

    private LicenseManager() throws OpenDataException, JAXBException {
        super(new IsOkAttribute(),
                new LicenseAttribute());
    }

    private static synchronized LicenseManager getInstanceSynchronized() throws OpenDataException, JAXBException {
        if(INSTANCE == null)
            INSTANCE = new LicenseManager();
        return INSTANCE;
    }

    public static LicenseManager getInstance() throws OpenDataException, JAXBException {
        return INSTANCE == null ? getInstanceSynchronized() : INSTANCE;
    }

    @Override
    public XmlLicense getLicense() throws JAXBException {
        final LicenseAttribute attributeDef = getAttributeInfo(LicenseAttribute.class);
        return attributeDef != null ? attributeDef.getLicense() : new XmlLicense();
    }

    /**
     * Gets license content tracker.
     * @return The license content tracker.
     */
    public LicenseLoader getLicenseLoader(){
        return getAttributeInfo(LicenseAttribute.class);
    }
}
