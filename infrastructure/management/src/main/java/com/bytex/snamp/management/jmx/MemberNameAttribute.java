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
final class MemberNameAttribute extends OpenMBean.OpenAttribute<String, SimpleType<String>> {
    private final ClusterMember clusterMember;

    MemberNameAttribute(){
        super("memberName", SimpleType.STRING);
        clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
    }

    @Override
    public String getValue() {
        return clusterMember.getName();
    }
}
