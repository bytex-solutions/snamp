package com.bytex.snamp.management.jmx;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
final class IsActiveNodeAttribute extends OpenMBean.OpenAttribute<Boolean, SimpleType<Boolean>> {
    private final ClusterMember clusterMember;

    IsActiveNodeAttribute(){
        super("isActiveNode", SimpleType.BOOLEAN);
        clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
    }

    @Override
    public boolean isIs() {
        return true;
    }

    @Override
    public Boolean getValue() {
        return clusterMember.isActive();
    }
}
