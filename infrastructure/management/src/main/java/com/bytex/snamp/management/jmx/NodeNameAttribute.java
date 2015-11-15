package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NodeNameAttribute extends OpenMBean.OpenAttribute<String, SimpleType<String>> {
    NodeNameAttribute(){
        super("nodeName", SimpleType.STRING);
    }

    @Override
    public String getValue() {
        return DistributedServices.getLocalNodeName(Utils.getBundleContextOfObject(this));
    }
}
