package com.itworks.snamp.management.webconsole;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static com.itworks.snamp.licensing.LicenseReader.LICENSE_CONTENT_ENTRY;
import static com.itworks.snamp.licensing.LicenseReader.LICENSE_CONTENT_ENCODING;
import static com.itworks.snamp.licensing.LicenseReader.LICENSE_PID;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class LicenseManager {
    private LicenseManager(){

    }

    static String getLicenseContent(final BundleContext context) throws IOException {
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef = new ServiceReferenceHolder<>(context,
                ConfigurationAdmin.class);
        try{
            final ConfigurationAdmin admin = adminRef.getService();
            final byte[] licenseContent = Utils.getProperty(admin.getConfiguration(LICENSE_PID, null).getProperties(),
                    LICENSE_CONTENT_ENTRY,
                    byte[].class, new byte[0]);
            return licenseContent == null || licenseContent.length == 0 ?
                    "":
                    new String(licenseContent, LICENSE_CONTENT_ENCODING);
        }
        finally {
            adminRef.release(context);
        }
    }

    static void setLicenseContent(final BundleContext context, final String licenseContent) throws IOException{
        if(licenseContent == null || licenseContent.isEmpty()) return;
        final ServiceReferenceHolder<ConfigurationAdmin> adminRef = new ServiceReferenceHolder<>(context,
                ConfigurationAdmin.class);
        try{
            final ConfigurationAdmin admin = adminRef.getService();
            final Configuration config = admin.getConfiguration(LICENSE_PID, null);
            final Dictionary<String, byte[]> dict = new Hashtable<>(1);
            dict.put(LICENSE_CONTENT_ENTRY, licenseContent.getBytes(LICENSE_CONTENT_ENCODING));
            config.update(dict);
        }
        finally {
            adminRef.release(context);
        }
    }
}
