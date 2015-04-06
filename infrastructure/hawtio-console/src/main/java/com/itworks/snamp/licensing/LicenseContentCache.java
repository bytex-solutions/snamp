package com.itworks.snamp.licensing;

import com.itworks.snamp.TimeSpan;
import com.itworks.snamp.concurrent.SimpleCache;
import com.itworks.snamp.concurrent.TemporaryCache;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Represents time-based license content.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class LicenseContentCache extends TemporaryCache<ConfigurationAdmin, XmlLicense, IOException> {


    LicenseContentCache() {
        super(2, TimeUnit.SECONDS);
    }

    @Override
    protected XmlLicense init(final ConfigurationAdmin configAdmin) throws IOException {
        return XmlLicense.readLicense(configAdmin);
    }

    @Override
    protected void expire(final ConfigurationAdmin input, final XmlLicense value) throws IOException {

    }

    public void reset(){
        invalidate();
    }
}
