package com.bytex.snamp.management.shell;

import com.bytex.snamp.core.ClusterMember;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
abstract class ClusterMemberCommand extends SnampShellCommand {
    final ClusterMember clusterMember;

    ClusterMemberCommand(){
        clusterMember = ClusterMember.get(getBundleContext());
    }
}
