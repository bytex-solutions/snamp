package com.itworks.snamp.licensing;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.openmbean.SimpleType;

import java.io.IOException;

import static com.itworks.snamp.jmx.OpenMBean.OpenAttribute;

/**
 * Exposes access to the SNAMP license content.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class LicenseAttribute extends OpenAttribute<String, SimpleType<String>> {
    private static final String NAME = "license";

    private final LicenseContentCache cache;

    LicenseAttribute() {
        super(NAME, SimpleType.STRING);
        cache = new LicenseContentCache();
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    @Override
    public String getValue() throws IOException {
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(getBundleContext(), ConfigurationAdmin.class);
        try{
            return XmlLicense.toString(cache.get(configAdminRef.getService()));
        }
        finally {
            configAdminRef.release(getBundleContext());
        }
    }

    @Override
    public void setValue(final String value) throws Exception {
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(getBundleContext(), ConfigurationAdmin.class);
        try{
            final XmlLicense license = XmlLicense.fromString(value);
            XmlLicense.writeLicense(configAdminRef.get(), license);
            cache.reset();
        }
        finally {
            configAdminRef.release(getBundleContext());
        }
    }
}
