package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.0
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
        return ClusterMember.isInCluster(Utils.getBundleContextOfObject(this));
    }
}
