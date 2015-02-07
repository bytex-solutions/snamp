package com.itworks.snamp.management.impl;

import com.itworks.snamp.management.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;
import java.io.IOException;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class LicenseAttribute extends OpenMBean.OpenAttribute<String, SimpleType<String>> {
    private static final String NAME = "license";

    LicenseAttribute(){
        super(NAME, SimpleType.STRING);
    }

    @Override
    public String getValue() throws IOException {
        return LicenseManager.getLicenseContent(getBundleContextByObject(this));
    }

    @Override
    public void setValue(final String licenseContent) throws IOException {
        LicenseManager.setLicenseContent(getBundleContextByObject(this), licenseContent);
    }

    @Override
    protected String getDescription() {
        return "SNAMP Configuration";
    }
}
