package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.PlatformVersion;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;

/**
 * JMX attribute with SNAMP platform version.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class PlatformVersionAttribute extends OpenMBean.OpenAttribute<String, SimpleType<String>> {

    PlatformVersionAttribute(){
        super("version", SimpleType.STRING);
    }

    @Override
    public String getValue() {
        return PlatformVersion.get().toString();
    }
}
