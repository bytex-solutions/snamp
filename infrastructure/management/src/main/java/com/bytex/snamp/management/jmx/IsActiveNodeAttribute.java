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
final class IsActiveNodeAttribute extends OpenMBean.OpenAttribute<Boolean, SimpleType<Boolean>> {
    IsActiveNodeAttribute(){
        super("isActiveNode", SimpleType.BOOLEAN);
    }

    @Override
    public boolean isIs() {
        return true;
    }

    @Override
    public Boolean getValue() {
        return DistributedServices.isActiveNode(Utils.getBundleContextOfObject(this));
    }
}
