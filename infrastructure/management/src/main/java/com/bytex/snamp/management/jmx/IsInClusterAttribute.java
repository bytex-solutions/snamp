package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class IsInClusterAttribute extends OpenMBean.OpenAttribute<Boolean, SimpleType<Boolean>> {
    IsInClusterAttribute(){
        super("isInCluster", SimpleType.BOOLEAN);
    }

    @Override
    public boolean isIs() {
        return true;
    }

    @Override
    public Boolean getValue() {
        return DistributedServices.isInCluster(Utils.getBundleContextOfObject(this));
    }
}
